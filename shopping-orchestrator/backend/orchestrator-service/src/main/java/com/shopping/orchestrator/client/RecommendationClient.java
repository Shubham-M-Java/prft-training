package com.shopping.orchestrator.client;

import com.shopping.orchestrator.dto.BudgetAnalysis;
import com.shopping.orchestrator.dto.ComparisonResult;
import com.shopping.orchestrator.dto.RecommendationResult;
import com.shopping.orchestrator.dto.ShoppingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "recommendation-agent", url = "${agents.recommendation.url:}")
public interface RecommendationClient {

    @PostMapping("/api/recommendation/recommend")
    RecommendationResult getRecommendation(@RequestBody ComparisonResult comparisonResult,
                                            @RequestParam("budget") Double budget,
                                            @RequestParam("preferences") String preferences);
}
