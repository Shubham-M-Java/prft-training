package com.shopping.comparison.controller;

import com.shopping.comparison.dto.ComparisonResult;
import com.shopping.comparison.dto.ProductInfo;
import com.shopping.comparison.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comparison")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComparisonController {

    private final ComparisonService comparisonService;

    @PostMapping("/compare")
    public ResponseEntity<ComparisonResult> compareProducts(@RequestBody List<ProductInfo> products) {
        return ResponseEntity.ok(comparisonService.compareProducts(products));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Comparison Agent is running!");
    }
}
