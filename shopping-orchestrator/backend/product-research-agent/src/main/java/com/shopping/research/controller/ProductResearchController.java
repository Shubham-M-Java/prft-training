package com.shopping.research.controller;

import com.shopping.research.dto.ProductInfo;
import com.shopping.research.service.ProductResearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductResearchController {

    private final ProductResearchService productResearchService;

    @PostMapping("/products")
    public ResponseEntity<List<ProductInfo>> researchProducts(@RequestBody Map<String, Object> request) {
        String category = (String) request.get("productCategory");
        Double budget = request.get("budget") != null ? ((Number) request.get("budget")).doubleValue() : null;
        String preferences = (String) request.get("preferences");
        String brand = (String) request.get("brand");

        log.info("Research request - Category: {}, Budget: {}, Preferences: {}", category, budget, preferences);
        List<ProductInfo> products = productResearchService.researchProducts(category, budget, preferences, brand);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{category}")
    public ResponseEntity<List<ProductInfo>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Double budget,
            @RequestParam(required = false) String brand) {
        List<ProductInfo> products = productResearchService.researchProducts(category, budget, null, brand);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Product Research Agent is running!");
    }
}
