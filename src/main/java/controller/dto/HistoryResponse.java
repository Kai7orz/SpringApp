package controller.dto;

import core.query.QueryHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private String sqlText;
    private Integer executionTimeMs;
    private Integer rowsScanned;
    private Integer rowsReturned;
    private String indexUsed;
    private String status;
    private LocalDateTime createdAt;

    public static HistoryResponse fromEntity(QueryHistory history) {
        return new HistoryResponse(
                history.getId().orElse(null),
                history.getSqlText(),
                history.getExecutionTimeMs(),
                history.getRowsScanned(),
                history.getRowsReturned(),
                history.getIndexUsed(),
                history.getStatus().name(),
                history.getCreatedAt()
        );
    }
}
