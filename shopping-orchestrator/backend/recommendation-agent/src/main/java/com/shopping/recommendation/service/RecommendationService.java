package com.shopping.recommendation.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductInfo {
        private String id;
        private String name;
        private String brand;
        private String category;
        private Double price;
        private String priceRange;
        private List<String> keyFeatures;
        private String notableHighlights;
        private Double rating;
        private String imageUrl;
        private String sourceUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ComparisonResult {
        private List<ProductInfo> products;
        private Map<String, List<String>> featureComparison;
        private Map<String, List<String>> prosAndCons;
        private String summary;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RecommendationResult {
        private ProductInfo bestProduct;
        private String reasonForRecommendation;
        private List<ProductInfo> alternativeOptions;
        private String finalDecision;
    }

    public RecommendationResult getRecommendation(ComparisonResult comparisonResult, Double budget, String preferences) {
        log.info("Generating recommendation for budget: {}, preferences: {}", budget, preferences);

        List<ProductInfo> products = comparisonResult.getProducts();
        if (products == null || products.isEmpty()) {
            return RecommendationResult.builder()
                    .finalDecision("No products available for recommendation")
                    .build();
        }

        // Score each product
        Map<ProductInfo, Double> scores = new HashMap<>();
        for (ProductInfo product : products) {
            double score = calculateScore(product, budget, preferences);
            scores.put(product, score);
        }

        // Sort by score
        List<ProductInfo> ranked = products.stream()
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b, 0.0), scores.getOrDefault(a, 0.0)))
                .collect(Collectors.toList());

        ProductInfo best = ranked.get(0);
        List<ProductInfo> alternatives = ranked.stream().skip(1).limit(2).collect(Collectors.toList());

        String reason = buildReason(best, budget, preferences, scores.get(best));
        String finalDecision = buildFinalDecision(best, alternatives);

        return RecommendationResult.builder()
                .bestProduct(best)
                .reasonForRecommendation(reason)
                .alternativeOptions(alternatives)
                .finalDecision(finalDecision)
                .build();
    }

    private double calculateScore(ProductInfo product, Double budget, String preferences) {
        double score = 0.0;

        // Rating score (40%)
        if (product.getRating() != null) {
            score += (product.getRating() / 5.0) * 40;
        }

        // Budget fit score (30%)
        if (product.getPrice() != null && budget != null) {
            if (product.getPrice() <= budget) {
                double budgetUtilization = product.getPrice() / budget;
                // Best score for products using 70-95% of budget
                if (budgetUtilization >= 0.7 && budgetUtilization <= 0.95) {
                    score += 30;
                } else if (budgetUtilization < 0.7) {
                    score += 20; // Cheaper but might miss features
                } else {
                    score += 25; // Slightly over budget
                }
            } else {
                score += 5; // Over budget penalty
            }
        }

        // Preferences match score (30%)
        if (preferences != null && !preferences.isEmpty() && product.getKeyFeatures() != null) {
            String prefLower = preferences.toLowerCase();
            long matchCount = product.getKeyFeatures().stream()
                    .filter(f -> f.toLowerCase().contains(prefLower) ||
                            prefLower.contains(f.toLowerCase().split(" ")[0]))
                    .count();
            score += Math.min(30, matchCount * 10);

            // Check notable highlights
            if (product.getNotableHighlights() != null &&
                    product.getNotableHighlights().toLowerCase().contains(prefLower)) {
                score += 10;
            }
        } else {
            score += 15; // Neutral if no preferences
        }

        return score;
    }

    private String buildReason(ProductInfo product, Double budget, String preferences, Double score) {
        StringBuilder reason = new StringBuilder();
        reason.append(product.getName()).append(" is recommended because: ");

        if (product.getRating() != null && product.getRating() >= 4.5) {
            reason.append("it has excellent user ratings (").append(product.getRating()).append("/5), ");
        }

        if (product.getPrice() != null && budget != null && product.getPrice() <= budget) {
            reason.append("fits within your budget of ₹").append(String.format("%.0f", budget))
                  .append(" at ₹").append(String.format("%.0f", product.getPrice())).append(", ");
        }

        if (product.getNotableHighlights() != null) {
            reason.append(product.getNotableHighlights()).append(". ");
        }

        if (product.getKeyFeatures() != null && !product.getKeyFeatures().isEmpty()) {
            reason.append("Key features include: ")
                  .append(String.join(", ", product.getKeyFeatures().subList(0, Math.min(3, product.getKeyFeatures().size()))));
        }

        return reason.toString();
    }

    private String buildFinalDecision(ProductInfo best, List<ProductInfo> alternatives) {
        StringBuilder decision = new StringBuilder();
        decision.append("🏆 Best Choice: ").append(best.getName());
        decision.append(" (").append(best.getBrand()).append(")");
        decision.append(" at ").append(best.getPriceRange() != null ? best.getPriceRange() : "₹" + best.getPrice());
        decision.append(" with rating ").append(best.getRating()).append("/5. ");

        if (!alternatives.isEmpty()) {
            decision.append("Alternatives: ");
            decision.append(alternatives.stream()
                    .map(p -> p.getName() + " (₹" + String.format("%.0f", p.getPrice()) + ")")
                    .collect(Collectors.joining(", ")));
        }

        return decision.toString();
    }
}
