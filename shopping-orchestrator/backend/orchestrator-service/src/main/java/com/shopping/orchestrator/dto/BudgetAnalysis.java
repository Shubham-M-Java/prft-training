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
public class BudgetAnalysis {
    private Double userBudget;
    private List<ProductInfo> productsWithinBudget;
    private List<ProductInfo> cheaperAlternatives;
    private String costVsValueInsight;
    private ProductInfo bestValueProduct;
}
