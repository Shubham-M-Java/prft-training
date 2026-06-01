package com.shopping.budget.service;

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
public class BudgetService {

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
    public static class BudgetAnalysis {
        private Double userBudget;
        private List<ProductInfo> productsWithinBudget;
        private List<ProductInfo> cheaperAlternatives;
        private String costVsValueInsight;
        private ProductInfo bestValueProduct;
    }

    public BudgetAnalysis analyzeBudget(List<ProductInfo> products, Double budget) {
        log.info("Analyzing budget: {} for {} products", budget, products.size());

        List<ProductInfo> withinBudget = products.stream()
                .filter(p -> p.getPrice() != null && p.getPrice() <= budget)
                .sorted(Comparator.comparingDouble((ProductInfo p) -> p.getRating() != null ? -p.getRating() : 0))
                .collect(Collectors.toList());

        List<ProductInfo> cheaperAlternatives = products.stream()
                .filter(p -> p.getPrice() != null && p.getPrice() <= budget * 0.7)
                .sorted(Comparator.comparingDouble((ProductInfo p) -> p.getRating() != null ? -p.getRating() : 0))
                .collect(Collectors.toList());

        ProductInfo bestValue = findBestValue(withinBudget, budget);
        String insight = buildInsight(products, withinBudget, budget);

        return BudgetAnalysis.builder()
                .userBudget(budget)
                .productsWithinBudget(withinBudget)
                .cheaperAlternatives(cheaperAlternatives)
                .costVsValueInsight(insight)
                .bestValueProduct(bestValue)
                .build();
    }

    private ProductInfo findBestValue(List<ProductInfo> withinBudget, Double budget) {
        if (withinBudget.isEmpty()) return null;
        return withinBudget.stream()
                .max(Comparator.comparingDouble(p -> {
                    double rating = p.getRating() != null ? p.getRating() : 0;
                    double priceScore = p.getPrice() != null ? (budget - p.getPrice()) / budget : 0;
                    return rating * 0.7 + priceScore * 0.3;
                }))
                .orElse(withinBudget.get(0));
    }

    private String buildInsight(List<ProductInfo> all, List<ProductInfo> withinBudget, Double budget) {
        StringBuilder sb = new StringBuilder();
        sb.append("Budget: ₹").append(String.format("%.0f", budget)).append(". ");
        sb.append(withinBudget.size()).append(" of ").append(all.size()).append(" products fit within budget. ");
        if (!withinBudget.isEmpty()) {
            ProductInfo cheapest = withinBudget.stream()
                    .min(Comparator.comparingDouble(p -> p.getPrice() != null ? p.getPrice() : Double.MAX_VALUE))
                    .orElse(withinBudget.get(0));
            sb.append("Most affordable option: ").append(cheapest.getName())
              .append(" at ₹").append(String.format("%.0f", cheapest.getPrice())).append(". ");
        }
        if (withinBudget.isEmpty()) {
            sb.append("Consider increasing budget or looking at alternatives. ");
            ProductInfo closest = all.stream()
                    .min(Comparator.comparingDouble(p -> p.getPrice() != null ? Math.abs(p.getPrice() - budget) : Double.MAX_VALUE))
                    .orElse(null);
            if (closest != null) {
                sb.append("Closest option: ").append(closest.getName())
                  .append(" at ₹").append(String.format("%.0f", closest.getPrice()));
            }
        }
        return sb.toString();
    }
}
