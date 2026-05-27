package com.shopping.orchestrator.client;

import com.shopping.orchestrator.dto.BudgetAnalysis;
import com.shopping.orchestrator.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "budget-agent", url = "${agents.budget.url:}")
public interface BudgetClient {

    @PostMapping("/api/budget/analyze")
    BudgetAnalysis analyzeBudget(@RequestBody List<ProductInfo> products,
                                  @RequestParam("budget") Double budget);
}
