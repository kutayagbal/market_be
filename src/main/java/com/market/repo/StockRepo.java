package com.market.repo;

import java.util.Optional;

import com.market.entity.Stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepo extends JpaRepository<Stock, Long> {
    Optional<Stock> findByName(String name);
}
