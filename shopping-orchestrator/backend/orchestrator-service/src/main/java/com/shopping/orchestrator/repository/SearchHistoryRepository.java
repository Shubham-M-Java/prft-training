package com.shopping.orchestrator.repository;

import com.shopping.orchestrator.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    Optional<SearchHistory> findByRequestId(String requestId);
    List<SearchHistory> findAllByOrderByCreatedAtDesc();
    List<SearchHistory> findByProductCategoryIgnoreCase(String productCategory);
}
