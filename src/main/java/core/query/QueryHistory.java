package core.query;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
public class QueryHistory {

    private Long id;
    private final Integer userId;
    private final String sqlText;
    private final Integer executionTimeMs;
    private final Integer rowsScanned;
    private final Integer rowsReturned;
    private final String indexUsed;
    private final String explainResult;
    private final Status status;
    private final LocalDateTime createdAt;

    public enum Status {
        SUCCESS, ERROR, TIMEOUT
    }

    public QueryHistory(Integer userId, String sqlText, Integer executionTimeMs,
                        Integer rowsScanned, Integer rowsReturned, String indexUsed,
                        String explainResult, Status status) {
        this(null, userId, sqlText, executionTimeMs, rowsScanned, rowsReturned,
                indexUsed, explainResult, status, LocalDateTime.now());
    }

    public QueryHistory(Long id, Integer userId, String sqlText, Integer executionTimeMs,
                        Integer rowsScanned, Integer rowsReturned, String indexUsed,
                        String explainResult, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.sqlText = sqlText;
        this.executionTimeMs = executionTimeMs;
        this.rowsScanned = rowsScanned;
        this.rowsReturned = rowsReturned;
        this.indexUsed = indexUsed;
        this.explainResult = explainResult;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Optional<Long> getId() {
        return Optional.ofNullable(id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static QueryHistory fromQueryResult(Integer userId,
                                               service.QueryExecutionService.QueryResult result) {
        Status status;
        switch (result.getStatus()) {
            case "SUCCESS":
                status = Status.SUCCESS;
                break;
            case "TIMEOUT":
                status = Status.TIMEOUT;
                break;
            default:
                status = Status.ERROR;
        }

        return new QueryHistory(
                userId,
                result.getOriginalSql(),
                result.getExecutionTimeMs() != null ? result.getExecutionTimeMs().intValue() : null,
                result.getRowsScanned(),
                result.getRowsReturned(),
                result.getIndexUsed(),
                result.getExplainResult(),
                status
        );
    }
}
