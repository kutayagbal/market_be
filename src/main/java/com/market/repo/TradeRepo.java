package com.market.repo;

import com.market.model.Trade;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepo extends JpaRepository<Trade, Long> {

}
