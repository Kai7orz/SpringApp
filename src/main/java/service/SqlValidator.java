package service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlValidator {

    private final int maxRows;

    // 禁止コマンドパターン（大文字小文字無視）
    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
            Pattern.compile("\\bDROP\\s+DATABASE\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bDROP\\s+SCHEMA\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bTRUNCATE\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bGRANT\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bREVOKE\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bCREATE\\s+USER\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bDROP\\s+USER\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bALTER\\s+USER\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bSHUTDOWN\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bLOAD\\s+DATA\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bINTO\\s+OUTFILE\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bINTO\\s+DUMPFILE\\b", Pattern.CASE_INSENSITIVE)
    );

    // 一般ユーザーに許可されるテーブル（sample_* テーブルのみ）
    private static final Set<String> ALLOWED_USER_TABLES = Set.of(
            "sample_customers",
            "sample_products",
            "sample_orders",
            "sample_order_items"
    );

    // 複文チェック用パターン
    private static final Pattern MULTI_STATEMENT_PATTERN = Pattern.compile(";\\s*\\S");

    // SELECTステートメント判定
    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\b", Pattern.CASE_INSENSITIVE);

    // EXPLAINステートメント判定
    private static final Pattern EXPLAIN_PATTERN = Pattern.compile("^\\s*EXPLAIN\\b", Pattern.CASE_INSENSITIVE);

    // LIMITチェック
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\bLIMIT\\s+\\d+", Pattern.CASE_INSENSITIVE);

    // テーブル名抽出（FROM句、JOIN句から）
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "(?:\\bFROM\\b|\\bJOIN\\b)\\s+([`\\w]+)",
            Pattern.CASE_INSENSITIVE
    );

    public SqlValidator(@Value("${query.max.rows:1000}") int maxRows) {
        this.maxRows = maxRows;
    }

    public ValidationResult validate(String sql, boolean isAdmin) {
        if (sql == null || sql.trim().isEmpty()) {
            return ValidationResult.error("SQL cannot be empty");
        }

        String trimmedSql = sql.trim();

        // 複文チェック
        if (MULTI_STATEMENT_PATTERN.matcher(trimmedSql).find()) {
            return ValidationResult.error("Multiple statements are not allowed");
        }

        // 禁止コマンドチェック
        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            if (pattern.matcher(trimmedSql).find()) {
                return ValidationResult.error("Forbidden SQL command detected");
            }
        }

        // EXPLAIN の場合は内部のSQLを検証
        if (EXPLAIN_PATTERN.matcher(trimmedSql).find()) {
            String innerSql = trimmedSql.replaceFirst("(?i)^\\s*EXPLAIN\\s+", "");
            return validate(innerSql, isAdmin);
        }

        // 一般ユーザーの場合
        if (!isAdmin) {
            // SELECTのみ許可
            if (!SELECT_PATTERN.matcher(trimmedSql).find()) {
                return ValidationResult.error("Only SELECT statements are allowed for non-admin users");
            }

            // sample_* テーブルのみ許可
            Matcher tableMatcher = TABLE_PATTERN.matcher(trimmedSql);
            while (tableMatcher.find()) {
                String tableName = tableMatcher.group(1).replace("`", "").toLowerCase();
                if (!ALLOWED_USER_TABLES.contains(tableName)) {
                    return ValidationResult.error(
                            "Access denied to table: " + tableName + ". Only sample_* tables are allowed."
                    );
                }
            }
        }

        // LIMIT強制付与
        String processedSql = ensureLimit(trimmedSql);

        return ValidationResult.success(processedSql);
    }

    private String ensureLimit(String sql) {
        // すでにLIMITがある場合
        Matcher limitMatcher = LIMIT_PATTERN.matcher(sql);
        if (limitMatcher.find()) {
            return sql;
        }

        // SELECTクエリの場合のみLIMIT追加
        if (SELECT_PATTERN.matcher(sql).find()) {
            // 末尾のセミコロンを除去してLIMIT追加
            String cleanSql = sql.replaceAll(";\\s*$", "");
            return cleanSql + " LIMIT " + maxRows;
        }

        return sql;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String processedSql;

        private ValidationResult(boolean valid, String errorMessage, String processedSql) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.processedSql = processedSql;
        }

        public static ValidationResult success(String processedSql) {
            return new ValidationResult(true, null, processedSql);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getProcessedSql() {
            return processedSql;
        }
    }
}
