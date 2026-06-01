package com.shopping.budget.controller;

import com.shopping.budget.service.BudgetService;
import com.shopping.budget.service.BudgetService.BudgetAnalysis;
import com.shopping.budget.service.BudgetService.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/analyze")
    public ResponseEntity<BudgetAnalysis> analyzeBudget(
            @RequestBody List<ProductInfo> products,
            @RequestParam("budget") Double budget) {
        return ResponseEntity.ok(budgetService.analyzeBudget(products, budget));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Budget Agent is running!");
    }
}
