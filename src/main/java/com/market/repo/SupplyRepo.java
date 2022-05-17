package com.market.repo;

import java.util.List;

import com.market.model.Stock;
import com.market.model.Supply;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyRepo extends JpaRepository<Supply, Long> {

    List<Supply> findAllByStock(Stock stock, Pageable pageable);

    List<Supply> findAllByStockOrderByPriceAsc(String stock);

}
