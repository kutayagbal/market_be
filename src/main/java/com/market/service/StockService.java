package com.market.service;

import java.util.List;
import java.util.stream.Collectors;

import com.market.model.Quote;
import com.market.model.QuotePriceHistory;
import com.market.repo.StockRepo;

import org.springframework.stereotype.Service;

@Service
public class StockService {
    private StockRepo stockRepo;

    public StockService(StockRepo stockRepo) {
        this.stockRepo = stockRepo;
    }

    public List<Quote> getStockQuotes() {
        return stockRepo.findAll().stream().map(stock -> new Quote(stock.getName(), stock.getPrice(),
                stock.getBestBid(), stock.getBestAsk(), stock.getRate(),
                stock.getPriceHistory().stream()
                        .map(priceHistory -> new QuotePriceHistory(priceHistory.getTime(), priceHistory.getPrice()))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
