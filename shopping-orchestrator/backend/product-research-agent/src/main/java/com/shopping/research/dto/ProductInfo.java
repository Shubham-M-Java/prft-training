package com.shopping.research.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {
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
    private String platform;          // "Amazon", "Flipkart", or "Mock"
    private Integer reviewCount;
    private String availability;
    private String discount;
}
