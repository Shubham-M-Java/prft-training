package com.shopping.recommendation.controller;

import com.shopping.recommendation.service.RecommendationService;
import com.shopping.recommendation.service.RecommendationService.ComparisonResult;
import com.shopping.recommendation.service.RecommendationService.RecommendationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResult> getRecommendation(
            @RequestBody ComparisonResult comparisonResult,
            @RequestParam("budget") Double budget,
            @RequestParam(value = "preferences", required = false, defaultValue = "") String preferences) {
        return ResponseEntity.ok(recommendationService.getRecommendation(comparisonResult, budget, preferences));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Recommendation Agent is running!");
    }
}
