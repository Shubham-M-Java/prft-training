package com.shopping.orchestrator.service;

import com.shopping.orchestrator.client.BudgetClient;
import com.shopping.orchestrator.client.ComparisonClient;
import com.shopping.orchestrator.client.ProductResearchClient;
import com.shopping.orchestrator.client.RecommendationClient;
import com.shopping.orchestrator.dto.*;
import com.shopping.orchestrator.entity.SearchHistory;
import com.shopping.orchestrator.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {

    private final ProductResearchClient productResearchClient;
    private final ComparisonClient comparisonClient;
    private final BudgetClient budgetClient;
    private final RecommendationClient recommendationClient;
    private final SearchHistoryRepository searchHistoryRepository;

    public OrchestratorResponse processShoppingRequest(ShoppingRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("Processing shopping request [{}] for category: {}, budget: {}",
                requestId, request.getProductCategory(), request.getBudget());

        try {
            // Step 1: Research Products
            log.info("[{}] Step 1: Calling Product Research Agent", requestId);
            List<ProductInfo> products = productResearchClient.researchProducts(request);
            log.info("[{}] Research Agent returned {} products", requestId, products.size());

            // Step 2: Compare Products
            log.info("[{}] Step 2: Calling Comparison Agent", requestId);
            ComparisonResult comparisonResult = comparisonClient.compareProducts(products);
            log.info("[{}] Comparison Agent completed analysis", requestId);

            // Step 3: Budget Analysis
            log.info("[{}] Step 3: Calling Budget Agent", requestId);
            BudgetAnalysis budgetAnalysis = budgetClient.analyzeBudget(products, request.getBudget());
            log.info("[{}] Budget Agent completed analysis", requestId);

            // Step 4: Get Recommendation
            log.info("[{}] Step 4: Calling Recommendation Agent", requestId);
            String preferences = request.getPreferences() != null ? request.getPreferences() : "";
            RecommendationResult recommendation = recommendationClient.getRecommendation(
                    comparisonResult, request.getBudget(), preferences);
            log.info("[{}] Recommendation Agent provided final recommendation: {}",
                    requestId, recommendation.getBestProduct() != null ? recommendation.getBestProduct().getName() : "N/A");

            // Save to history
            saveSearchHistory(requestId, request, recommendation);

            return OrchestratorResponse.builder()
                    .requestId(requestId)
                    .timestamp(LocalDateTime.now())
                    .productCategory(request.getProductCategory())
                    .budget(request.getBudget())
                    .preferences(request.getPreferences())
                    .topOptionsFound(products)
                    .comparisonSummary(comparisonResult)
                    .budgetAnalysis(budgetAnalysis)
                    .finalRecommendation(recommendation)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("[{}] Error processing shopping request: {}", requestId, e.getMessage(), e);
            saveErrorHistory(requestId, request, e.getMessage());
            return OrchestratorResponse.builder()
                    .requestId(requestId)
                    .timestamp(LocalDateTime.now())
                    .productCategory(request.getProductCategory())
                    .budget(request.getBudget())
                    .preferences(request.getPreferences())
                    .status("ERROR")
                    .errorMessage("Failed to process request: " + e.getMessage())
                    .build();
        }
    }

    public List<SearchHistory> getSearchHistory() {
        return searchHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SearchHistory> getSearchHistoryByCategory(String category) {
        return searchHistoryRepository.findByProductCategoryIgnoreCase(category);
    }

    private void saveSearchHistory(String requestId, ShoppingRequest request, RecommendationResult recommendation) {
        try {
            SearchHistory history = SearchHistory.builder()
                    .requestId(requestId)
                    .productCategory(request.getProductCategory())
                    .budget(request.getBudget())
                    .preferences(request.getPreferences())
                    .bestProductName(recommendation.getBestProduct() != null ? recommendation.getBestProduct().getName() : null)
                    .bestProductPrice(recommendation.getBestProduct() != null ? recommendation.getBestProduct().getPrice() : null)
                    .createdAt(LocalDateTime.now())
                    .status("SUCCESS")
                    .build();
            searchHistoryRepository.save(history);
        } catch (Exception e) {
            log.warn("Failed to save search history: {}", e.getMessage());
        }
    }

    private void saveErrorHistory(String requestId, ShoppingRequest request, String errorMessage) {
        try {
            SearchHistory history = SearchHistory.builder()
                    .requestId(requestId)
                    .productCategory(request.getProductCategory())
                    .budget(request.getBudget())
                    .preferences(request.getPreferences())
                    .createdAt(LocalDateTime.now())
                    .status("ERROR")
                    .build();
            searchHistoryRepository.save(history);
        } catch (Exception e) {
            log.warn("Failed to save error history: {}", e.getMessage());
        }
    }
}
