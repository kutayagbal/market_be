package com.market.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.market.entity.Stock;
import com.market.entity.Supply;

public interface SupplyRepo extends JpaRepository<Supply, Long> {

    List<Supply> findAllByStock(Stock stock, Pageable pageable);

    List<Supply> findAllByStockOrderByPriceAsc(Stock stock);

}
