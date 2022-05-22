package com.market.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.market.common.Constants;
import com.market.common.MarketException;
import com.market.model.Order;
import com.market.service.OrderBookService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/admin/orderbook")
public class OrderBookController {
	private final OrderBookService service;

	public OrderBookController(OrderBookService service) {
		this.service = service;
	}

	@GetMapping("/demands")
	public List<Order> demands(@RequestParam String stock, @RequestParam(required = false) Short page) {
		return service.getAllDemands(stock, validatePage(page)).stream()
				.map(demand -> new Order(stock, demand.getQuantity(), demand.getPrice()))
				.collect(Collectors.toList());
	}

	@GetMapping("/supplies")
	public List<Order> supplies(@RequestParam String stock, @RequestParam(required = false) Short page) {
		return service.getAllSupplies(stock, validatePage(page)).stream()
				.map(supply -> new Order(stock, supply.getQuantity(), supply.getPrice()))
				.collect(Collectors.toList());
	}

	private short validatePage(Short page) {
		if (page == null)
			return 0;

		if (page.compareTo((short) 0) < 0
				|| page.compareTo(Constants.ORDER_LIST_PAGE_NUMBER_MAX_SIZE) >= 0) {
			throw new MarketException(Constants.INV_PAGE_MSG);
		}

		return page;
	}
}
