package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class QueryExecutionService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final SqlValidator sqlValidator;
    private final ObjectMapper objectMapper;
    private final int queryTimeoutSeconds;
    private final ExecutorService executorService;

    public QueryExecutionService(
            DataSource dataSource,
            SqlValidator sqlValidator,
            @Value("${query.timeout.seconds:30}") int queryTimeoutSeconds) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sqlValidator = sqlValidator;
        this.objectMapper = new ObjectMapper();
        this.queryTimeoutSeconds = queryTimeoutSeconds;
        this.executorService = Executors.newCachedThreadPool();
    }

    public QueryResult executeQuery(String sql, boolean isAdmin) {
        // SQL検証
        SqlValidator.ValidationResult validation = sqlValidator.validate(sql, isAdmin);
        if (!validation.isValid()) {
            return QueryResult.error(sql, validation.getErrorMessage());
        }

        String processedSql = validation.getProcessedSql();
        long startTime = System.currentTimeMillis();

        try {
            // タイムアウト付きで実行
            Future<QueryResult> future = executorService.submit(() ->
                    executeWithMetrics(processedSql, sql)
            );

            return future.get(queryTimeoutSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return QueryResult.timeout(sql, executionTime);
        } catch (InterruptedException | ExecutionException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return QueryResult.error(sql, cause.getMessage(), executionTime);
        }
    }

    private QueryResult executeWithMetrics(String processedSql, String originalSql) {
        long startTime = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // EXPLAIN取得
            List<Map<String, Object>> explainResult = getExplainResult(connection, processedSql);
            String explainJson = toJson(explainResult);

            // インデックス使用状況を抽出
            String indexUsed = extractIndexUsed(explainResult);
            Integer rowsScanned = extractRowsScanned(explainResult);

            // クエリ実行
            List<Map<String, Object>> data = new ArrayList<>();
            List<String> columns = new ArrayList<>();
            int rowsReturned = 0;

            try (Statement stmt = connection.createStatement()) {
                stmt.setQueryTimeout(queryTimeoutSeconds);

                try (ResultSet rs = stmt.executeQuery(processedSql)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // カラム名取得
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metaData.getColumnLabel(i));
                    }

                    // データ取得
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(columns.get(i - 1), rs.getObject(i));
                        }
                        data.add(row);
                        rowsReturned++;
                    }
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // ロールバック（SELECT のみなので変更はないが念のため）
            connection.rollback();

            return QueryResult.success(
                    originalSql,
                    processedSql,
                    columns,
                    data,
                    executionTime,
                    rowsReturned,
                    rowsScanned,
                    indexUsed,
                    explainJson
            );

        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return QueryResult.error(originalSql, "SQL Error: " + e.getMessage(), executionTime);
        }
    }

    public ExplainResult getExplainOnly(String sql, boolean isAdmin) {
        SqlValidator.ValidationResult validation = sqlValidator.validate(sql, isAdmin);
        if (!validation.isValid()) {
            return ExplainResult.error(validation.getErrorMessage());
        }

        String processedSql = validation.getProcessedSql();

        try (Connection connection = dataSource.getConnection()) {
            List<Map<String, Object>> explainResult = getExplainResult(connection, processedSql);
            String indexUsed = extractIndexUsed(explainResult);
            Integer rowsScanned = extractRowsScanned(explainResult);

            return ExplainResult.success(explainResult, indexUsed, rowsScanned);
        } catch (SQLException e) {
            return ExplainResult.error("SQL Error: " + e.getMessage());
        }
    }

    public List<QueryResult> compareQueries(List<String> sqls, boolean isAdmin) {
        List<QueryResult> results = new ArrayList<>();
        for (String sql : sqls) {
            results.add(executeQuery(sql, isAdmin));
        }
        return results;
    }

    private List<Map<String, Object>> getExplainResult(Connection connection, String sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("EXPLAIN " + sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
        }

        return result;
    }

    private String extractIndexUsed(List<Map<String, Object>> explainResult) {
        Set<String> indexes = new LinkedHashSet<>();
        for (Map<String, Object> row : explainResult) {
            Object key = row.get("key");
            if (key != null && !key.toString().isEmpty()) {
                indexes.add(key.toString());
            }
        }
        return indexes.isEmpty() ? null : String.join(", ", indexes);
    }

    private Integer extractRowsScanned(List<Map<String, Object>> explainResult) {
        int total = 0;
        for (Map<String, Object> row : explainResult) {
            Object rows = row.get("rows");
            if (rows instanceof Number) {
                total += ((Number) rows).intValue();
            }
        }
        return total > 0 ? total : null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    // 結果クラス
    public static class QueryResult {
        private final String status; // SUCCESS, ERROR, TIMEOUT
        private final String originalSql;
        private final String processedSql;
        private final List<String> columns;
        private final List<Map<String, Object>> data;
        private final Long executionTimeMs;
        private final Integer rowsReturned;
        private final Integer rowsScanned;
        private final String indexUsed;
        private final String explainResult;
        private final String errorMessage;

        private QueryResult(String status, String originalSql, String processedSql,
                            List<String> columns, List<Map<String, Object>> data,
                            Long executionTimeMs, Integer rowsReturned, Integer rowsScanned,
                            String indexUsed, String explainResult, String errorMessage) {
            this.status = status;
            this.originalSql = originalSql;
            this.processedSql = processedSql;
            this.columns = columns;
            this.data = data;
            this.executionTimeMs = executionTimeMs;
            this.rowsReturned = rowsReturned;
            this.rowsScanned = rowsScanned;
            this.indexUsed = indexUsed;
            this.explainResult = explainResult;
            this.errorMessage = errorMessage;
        }

        public static QueryResult success(String originalSql, String processedSql,
                                          List<String> columns, List<Map<String, Object>> data,
                                          long executionTimeMs, int rowsReturned,
                                          Integer rowsScanned, String indexUsed, String explainResult) {
            return new QueryResult("SUCCESS", originalSql, processedSql, columns, data,
                    executionTimeMs, rowsReturned, rowsScanned, indexUsed, explainResult, null);
        }

        public static QueryResult error(String originalSql, String errorMessage) {
            return new QueryResult("ERROR", originalSql, null, null, null,
                    null, null, null, null, null, errorMessage);
        }

        public static QueryResult error(String originalSql, String errorMessage, long executionTimeMs) {
            return new QueryResult("ERROR", originalSql, null, null, null,
                    executionTimeMs, null, null, null, null, errorMessage);
        }

        public static QueryResult timeout(String originalSql, long executionTimeMs) {
            return new QueryResult("TIMEOUT", originalSql, null, null, null,
                    executionTimeMs, null, null, null, null, "Query execution timed out");
        }

        // Getters
        public String getStatus() { return status; }
        public String getOriginalSql() { return originalSql; }
        public String getProcessedSql() { return processedSql; }
        public List<String> getColumns() { return columns; }
        public List<Map<String, Object>> getData() { return data; }
        public Long getExecutionTimeMs() { return executionTimeMs; }
        public Integer getRowsReturned() { return rowsReturned; }
        public Integer getRowsScanned() { return rowsScanned; }
        public String getIndexUsed() { return indexUsed; }
        public String getExplainResult() { return explainResult; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccess() { return "SUCCESS".equals(status); }
    }

    public static class ExplainResult {
        private final boolean success;
        private final List<Map<String, Object>> explainData;
        private final String indexUsed;
        private final Integer rowsScanned;
        private final String errorMessage;

        private ExplainResult(boolean success, List<Map<String, Object>> explainData,
                              String indexUsed, Integer rowsScanned, String errorMessage) {
            this.success = success;
            this.explainData = explainData;
            this.indexUsed = indexUsed;
            this.rowsScanned = rowsScanned;
            this.errorMessage = errorMessage;
        }

        public static ExplainResult success(List<Map<String, Object>> explainData,
                                            String indexUsed, Integer rowsScanned) {
            return new ExplainResult(true, explainData, indexUsed, rowsScanned, null);
        }

        public static ExplainResult error(String message) {
            return new ExplainResult(false, null, null, null, message);
        }

        public boolean isSuccess() { return success; }
        public List<Map<String, Object>> getExplainData() { return explainData; }
        public String getIndexUsed() { return indexUsed; }
        public Integer getRowsScanned() { return rowsScanned; }
        public String getErrorMessage() { return errorMessage; }
    }
}
