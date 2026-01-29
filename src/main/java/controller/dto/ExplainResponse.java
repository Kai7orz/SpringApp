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
public class ExplainResponse {
    private boolean success;
    private List<Map<String, Object>> explainData;
    private String indexUsed;
    private Integer rowsScanned;
    private String errorMessage;

    public static ExplainResponse fromResult(QueryExecutionService.ExplainResult result) {
        return new ExplainResponse(
                result.isSuccess(),
                result.getExplainData(),
                result.getIndexUsed(),
                result.getRowsScanned(),
                result.getErrorMessage()
        );
    }
}
