package com.shopping.orchestrator.client;

import com.shopping.orchestrator.dto.ProductInfo;
import com.shopping.orchestrator.dto.ShoppingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-research-agent", url = "${agents.research.url:}")
public interface ProductResearchClient {

    @PostMapping("/api/research/products")
    List<ProductInfo> researchProducts(@RequestBody ShoppingRequest request);
}
