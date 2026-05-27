package com.shopping.orchestrator.client;

import com.shopping.orchestrator.dto.ComparisonResult;
import com.shopping.orchestrator.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "comparison-agent", url = "${agents.comparison.url:}")
public interface ComparisonClient {

    @PostMapping("/api/comparison/compare")
    ComparisonResult compareProducts(@RequestBody List<ProductInfo> products);
}
