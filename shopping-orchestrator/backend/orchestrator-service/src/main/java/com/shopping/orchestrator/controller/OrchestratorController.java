package com.shopping.orchestrator.controller;

import com.shopping.orchestrator.dto.OrchestratorResponse;
import com.shopping.orchestrator.dto.ShoppingRequest;
import com.shopping.orchestrator.entity.SearchHistory;
import com.shopping.orchestrator.service.OrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    /**
     * Main endpoint - processes a full shopping request through all agents
     */
    @PostMapping("/search")
    public ResponseEntity<OrchestratorResponse> processShoppingRequest(
            @Valid @RequestBody ShoppingRequest request) {
        log.info("Received shopping request for: {} with budget: {}",
                request.getProductCategory(), request.getBudget());
        OrchestratorResponse response = orchestratorService.processShoppingRequest(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get search history
     */
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistory>> getSearchHistory() {
        return ResponseEntity.ok(orchestratorService.getSearchHistory());
    }

    /**
     * Get search history by category
     */
    @GetMapping("/history/{category}")
    public ResponseEntity<List<SearchHistory>> getSearchHistoryByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(orchestratorService.getSearchHistoryByCategory(category));
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Orchestrator Service is running!");
    }
}
