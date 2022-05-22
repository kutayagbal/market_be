package com.market.controller;

import java.util.List;

import com.market.common.Constants;
import com.market.common.MarketException;
import com.market.model.Quote;
import com.market.service.StockService;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
@CrossOrigin
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stocks")
    public List<Quote> getStockQuotes(@RequestParam String username) {
        validateUsername(username);
        return stockService.getStockQuotes();
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new MarketException(Constants.NO_USR_MSG);
        }
    }
}
