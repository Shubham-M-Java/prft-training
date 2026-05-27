package com.shopping.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult {
    private ProductInfo bestProduct;
    private String reasonForRecommendation;
    private List<ProductInfo> alternativeOptions;
    private String finalVerdict;
}
