package com.market.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.market.common.Constants;
import com.market.model.Demand;
import com.market.model.Stock;
import com.market.model.Supply;
import com.market.repo.DemandRepo;
import com.market.repo.StockRepo;
import com.market.repo.SupplyRepo;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OrderBookService {
	private DemandRepo demandRepo;
	private SupplyRepo supplyRepo;
	private StockRepo stockRepo;

	public OrderBookService(DemandRepo demandRepo, SupplyRepo supplyRepo) {
		super();
		this.demandRepo = demandRepo;
		this.supplyRepo = supplyRepo;
	}

	public List<Demand> getAllDemands(String stock, int page) {
		Optional<Stock> stockOpt = stockRepo.findByName(stock);

		if (stockOpt.isPresent()) {
			return demandRepo.findAllByStock(stockOpt.get(),
					PageRequest.of(page, Constants.ORDER_LIST_PAGE_SIZE, Sort.by("id").ascending()));
		}

		return Collections.emptyList();
	}

	public List<Supply> getAllSupplies(String stock, int page) {
		Optional<Stock> stockOpt = stockRepo.findByName(stock);

		if (stockOpt.isPresent()) {
			return supplyRepo.findAllByStock(stockOpt.get(),
					PageRequest.of(page, Constants.ORDER_LIST_PAGE_SIZE, Sort.by("id").ascending()));
		}

		return Collections.emptyList();
	}

}
