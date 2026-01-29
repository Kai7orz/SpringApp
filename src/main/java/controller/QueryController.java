package controller;

import controller.dto.*;
import core.query.QueryHistory;
import core.query.QueryHistoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import security.CustomUserDetails;
import service.QueryExecutionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryExecutionService queryExecutionService;
    private final QueryHistoryRepository queryHistoryRepository;

    public QueryController(QueryExecutionService queryExecutionService,
                           QueryHistoryRepository queryHistoryRepository) {
        this.queryExecutionService = queryExecutionService;
        this.queryHistoryRepository = queryHistoryRepository;
    }

    @PostMapping("/execute")
    public ResponseEntity<QueryResponse> executeQuery(
            @Valid @RequestBody QueryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        QueryExecutionService.QueryResult result =
                queryExecutionService.executeQuery(request.getSql(), userDetails.isAdmin());

        // 履歴保存
        saveHistory(userDetails.getId(), result);

        return ResponseEntity.ok(QueryResponse.fromResult(result));
    }

    @PostMapping("/explain")
    public ResponseEntity<ExplainResponse> explainQuery(
            @Valid @RequestBody QueryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        QueryExecutionService.ExplainResult result =
                queryExecutionService.getExplainOnly(request.getSql(), userDetails.isAdmin());

        return ResponseEntity.ok(ExplainResponse.fromResult(result));
    }

    @PostMapping("/compare")
    public ResponseEntity<List<QueryResponse>> compareQueries(
            @Valid @RequestBody CompareRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<QueryExecutionService.QueryResult> results =
                queryExecutionService.compareQueries(request.getQueries(), userDetails.isAdmin());

        // 各クエリの履歴を保存
        for (QueryExecutionService.QueryResult result : results) {
            saveHistory(userDetails.getId(), result);
        }

        List<QueryResponse> responses = results.stream()
                .map(QueryResponse::fromResult)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private void saveHistory(Integer userId, QueryExecutionService.QueryResult result) {
        try {
            QueryHistory history = QueryHistory.fromQueryResult(userId, result);
            queryHistoryRepository.save(history);
        } catch (Exception e) {
            // 履歴保存失敗はログのみ（クエリ実行には影響させない）
            System.err.println("Failed to save query history: " + e.getMessage());
        }
    }
}
