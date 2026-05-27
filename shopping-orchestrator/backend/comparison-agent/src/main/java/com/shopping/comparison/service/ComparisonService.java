package com.shopping.comparison.service;

import com.shopping.comparison.dto.ComparisonResult;
import com.shopping.comparison.dto.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComparisonService {

    public ComparisonResult compareProducts(List<ProductInfo> products) {
        log.info("Comparing {} products", products.size());

        if (products == null || products.isEmpty()) {
            return ComparisonResult.builder()
                    .products(Collections.emptyList())
                    .featureComparison(Collections.emptyMap())
                    .prosAndCons(Collections.emptyMap())
                    .summary("No products to compare")
                    .build();
        }

        Map<String, List<String>> featureComparison = buildFeatureComparison(products);
        Map<String, List<String>> prosAndCons = buildProsAndCons(products);
        String summary = buildSummary(products);

        return ComparisonResult.builder()
                .products(products)
                .featureComparison(featureComparison)
                .prosAndCons(prosAndCons)
                .summary(summary)
                .build();
    }

    private Map<String, List<String>> buildFeatureComparison(List<ProductInfo> products) {
        Map<String, List<String>> comparison = new LinkedHashMap<>();

        // Price comparison
        List<String> prices = products.stream()
                .map(p -> p.getName() + ": " + (p.getPriceRange() != null ? p.getPriceRange() : "₹" + p.getPrice()))
                .collect(Collectors.toList());
        comparison.put("Price", prices);

        // Rating comparison
        List<String> ratings = products.stream()
                .map(p -> p.getName() + ": " + p.getRating() + "/5.0")
                .collect(Collectors.toList());
        comparison.put("Rating", ratings);

        // Brand comparison
        List<String> brands = products.stream()
                .map(p -> p.getName() + ": " + p.getBrand())
                .collect(Collectors.toList());
        comparison.put("Brand", brands);

        // Key features
        List<String> features = products.stream()
                .map(p -> p.getName() + ": " + (p.getKeyFeatures() != null ?
                        String.join(", ", p.getKeyFeatures().subList(0, Math.min(3, p.getKeyFeatures().size()))) : "N/A"))
                .collect(Collectors.toList());
        comparison.put("Key Features", features);

        // Notable highlights
        List<String> highlights = products.stream()
                .map(p -> p.getName() + ": " + (p.getNotableHighlights() != null ? p.getNotableHighlights() : "N/A"))
                .collect(Collectors.toList());
        comparison.put("Notable Highlights", highlights);

        return comparison;
    }

    private Map<String, List<String>> buildProsAndCons(List<ProductInfo> products) {
        Map<String, List<String>> prosAndCons = new LinkedHashMap<>();

        for (ProductInfo product : products) {
            List<String> pros = new ArrayList<>();
            List<String> cons = new ArrayList<>();

            // Generate pros based on rating
            if (product.getRating() >= 4.7) {
                pros.add("Excellent user ratings (" + product.getRating() + "/5)");
            } else if (product.getRating() >= 4.4) {
                pros.add("Very good user ratings (" + product.getRating() + "/5)");
            }

            // Add feature-based pros
            if (product.getKeyFeatures() != null && !product.getKeyFeatures().isEmpty()) {
                pros.add("Rich feature set: " + product.getKeyFeatures().get(0));
                if (product.getKeyFeatures().size() > 1) {
                    pros.add(product.getKeyFeatures().get(1));
                }
            }

            if (product.getNotableHighlights() != null) {
                pros.add(product.getNotableHighlights());
            }

            // Generate cons based on price
            if (product.getPrice() != null) {
                if (product.getPrice() > 100000) {
                    cons.add("Premium pricing - may not suit all budgets");
                } else if (product.getPrice() > 50000) {
                    cons.add("Mid-to-high price range");
                } else {
                    cons.add("Entry-level specs compared to premium models");
                }
            }

            // Brand-specific cons
            if ("Apple".equalsIgnoreCase(product.getBrand())) {
                cons.add("Ecosystem lock-in with Apple products");
                cons.add("Limited customization options");
            } else if ("Samsung".equalsIgnoreCase(product.getBrand())) {
                cons.add("Bloatware on some models");
            } else if ("OnePlus".equalsIgnoreCase(product.getBrand())) {
                cons.add("Limited after-sales service centers");
            }

            prosAndCons.put(product.getName() + " - PROS", pros);
            prosAndCons.put(product.getName() + " - CONS", cons);
        }

        return prosAndCons;
    }

    private String buildSummary(List<ProductInfo> products) {
        if (products.isEmpty()) return "No products to summarize";

        ProductInfo topRated = products.stream()
                .max(Comparator.comparingDouble(p -> p.getRating() != null ? p.getRating() : 0))
                .orElse(products.get(0));

        ProductInfo cheapest = products.stream()
                .min(Comparator.comparingDouble(p -> p.getPrice() != null ? p.getPrice() : Double.MAX_VALUE))
                .orElse(products.get(0));

        StringBuilder summary = new StringBuilder();
        summary.append("Compared ").append(products.size()).append(" products. ");
        summary.append("Top rated: ").append(topRated.getName())
                .append(" (").append(topRated.getRating()).append("/5). ");
        summary.append("Most affordable: ").append(cheapest.getName())
                .append(" at ").append(cheapest.getPriceRange() != null ? cheapest.getPriceRange() : "₹" + cheapest.getPrice()).append(". ");
        summary.append("All products offer competitive features in their respective price segments.");

        return summary.toString();
    }
}
