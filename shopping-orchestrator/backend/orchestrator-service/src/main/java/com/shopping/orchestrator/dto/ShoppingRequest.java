package com.shopping.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingRequest {

    @NotBlank(message = "Product category is required")
    private String productCategory;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be positive")
    private Double budget;

    private String preferences;

    private String brand;

    private String additionalRequirements;
}
