package controller;

import controller.dto.HistoryResponse;
import controller.dto.PagedResponse;
import core.query.QueryHistory;
import core.query.QueryHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final QueryHistoryRepository queryHistoryRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public HistoryController(QueryHistoryRepository queryHistoryRepository) {
        this.queryHistoryRepository = queryHistoryRepository;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<HistoryResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // ページサイズ制限
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int offset = page * pageSize;

        List<QueryHistory> histories = queryHistoryRepository.findByUserId(
                userDetails.getId(), pageSize, offset);

        int totalCount = queryHistoryRepository.countByUserId(userDetails.getId());

        List<HistoryResponse> responses = histories.stream()
                .map(HistoryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(PagedResponse.of(responses, page, pageSize, totalCount));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHistoryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return queryHistoryRepository.findById(id)
                .filter(h -> h.getUserId().equals(userDetails.getId()))
                .map(h -> ResponseEntity.ok(HistoryResponse.fromEntity(h)))
                .orElse(ResponseEntity.notFound().build());
    }
}
