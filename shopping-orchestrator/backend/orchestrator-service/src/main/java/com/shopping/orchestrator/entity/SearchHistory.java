package com.shopping.orchestrator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true)
    private String requestId;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "budget")
    private Double budget;

    @Column(name = "preferences")
    private String preferences;

    @Column(name = "best_product_name")
    private String bestProductName;

    @Column(name = "best_product_price")
    private Double bestProductPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status;
}
