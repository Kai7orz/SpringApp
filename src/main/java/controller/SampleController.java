package controller;

import controller.dto.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.DataGeneratorService;

import java.util.Map;

@RestController
@RequestMapping("/api/sample")
@PreAuthorize("hasRole('ADMIN')")
public class SampleController {

    private final DataGeneratorService dataGeneratorService;

    public SampleController(DataGeneratorService dataGeneratorService) {
        this.dataGeneratorService = dataGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateSampleData(
            @RequestParam(defaultValue = "10000") int customers,
            @RequestParam(defaultValue = "1000") int products,
            @RequestParam(defaultValue = "50000") int orders,
            @RequestParam(defaultValue = "3") int itemsPerOrder) {

        // 制限チェック
        if (customers > 1000000 || products > 100000 || orders > 5000000) {
            return ResponseEntity.badRequest()
                    .body(new ApiError(400, "Data limits exceeded. Max: 1M customers, 100K products, 5M orders"));
        }

        try {
            // 非同期で実行
            new Thread(() -> {
                try {
                    dataGeneratorService.generateAllData(customers, products, orders, itemsPerOrder);
                } catch (Exception e) {
                    System.err.println("Data generation failed: " + e.getMessage());
                }
            }).start();

            return ResponseEntity.accepted().body(Map.of(
                    "message", "Data generation started",
                    "customers", customers,
                    "products", products,
                    "orders", orders,
                    "itemsPerOrder", itemsPerOrder
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiError(400, e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getGenerationStatus() {
        DataGeneratorService.GenerationStatus status = dataGeneratorService.getStatus();
        return ResponseEntity.ok(Map.of(
                "isGenerating", status.isGenerating(),
                "progress", status.getProgress(),
                "currentTask", status.getCurrentTask()
        ));
    }

    @GetMapping("/counts")
    public ResponseEntity<?> getTableCounts() {
        // テーブルのレコード数を取得（JdbcTemplateを直接使わないので、別途実装が必要）
        return ResponseEntity.ok(Map.of(
                "message", "Use SQL queries to check table counts",
                "hint", "SELECT 'customers' as tbl, COUNT(*) as cnt FROM sample_customers UNION ALL SELECT 'products', COUNT(*) FROM sample_products..."
        ));
    }
}
