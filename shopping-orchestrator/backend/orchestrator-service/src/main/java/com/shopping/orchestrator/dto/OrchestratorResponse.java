package com.shopping.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestratorResponse {

    private String requestId;
    private LocalDateTime timestamp;

    // User Requirement Section
    private String productCategory;
    private Double budget;
    private String preferences;

    // Research Agent Output
    private List<ProductInfo> topOptionsFound;

    // Comparison Agent Output
    private ComparisonResult comparisonSummary;

    // Budget Agent Output
    private BudgetAnalysis budgetAnalysis;

    // Recommendation Agent Output
    private RecommendationResult finalRecommendation;

    private String status;
    private String errorMessage;
}
