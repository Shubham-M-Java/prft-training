package com.shopping.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResult {
    private List<ProductInfo> products;
    private Map<String, List<String>> featureComparison;
    private Map<String, List<String>> prosAndCons;
    private String summary;
}
