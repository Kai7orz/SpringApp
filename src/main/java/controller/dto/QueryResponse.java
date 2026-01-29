package controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import service.QueryExecutionService;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    private String status;
    private String originalSql;
    private String processedSql;
    private List<String> columns;
    private List<Map<String, Object>> data;
    private Long executionTimeMs;
    private Integer rowsReturned;
    private Integer rowsScanned;
    private String indexUsed;
    private Object explainResult;
    private String errorMessage;

    public static QueryResponse fromResult(QueryExecutionService.QueryResult result) {
        QueryResponse response = new QueryResponse();
        response.setStatus(result.getStatus());
        response.setOriginalSql(result.getOriginalSql());
        response.setProcessedSql(result.getProcessedSql());
        response.setColumns(result.getColumns());
        response.setData(result.getData());
        response.setExecutionTimeMs(result.getExecutionTimeMs());
        response.setRowsReturned(result.getRowsReturned());
        response.setRowsScanned(result.getRowsScanned());
        response.setIndexUsed(result.getIndexUsed());
        response.setErrorMessage(result.getErrorMessage());

        // EXPLAIN結果をJSONからパース
        if (result.getExplainResult() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper =
                        new com.fasterxml.jackson.databind.ObjectMapper();
                response.setExplainResult(mapper.readValue(result.getExplainResult(), List.class));
            } catch (Exception e) {
                response.setExplainResult(result.getExplainResult());
            }
        }

        return response;
    }
}
