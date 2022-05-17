package com.market.repo;

import java.util.List;

import com.market.model.Demand;
import com.market.model.Stock;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandRepo extends JpaRepository<Demand, Long> {
    List<Demand> findAllByStock(Stock stock, Pageable pageable);

    List<Demand> findAllByStockOrderByPriceDesc(Stock stock);

}
