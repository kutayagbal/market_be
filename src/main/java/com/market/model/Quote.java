package com.market.model;

import java.math.BigDecimal;
import java.util.List;

public class Quote {
    private String name;
    private BigDecimal price;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal rate;

    List<QuotePriceHistory> priceHistory;

    public Quote(String name, BigDecimal price, BigDecimal bestBid, BigDecimal bestAsk,
            BigDecimal rate, List<QuotePriceHistory> priceHistory) {
        this.name = name;
        this.price = price;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.rate = rate;
        this.priceHistory = priceHistory;
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public BigDecimal getBestBid() {
        return this.bestBid;
    }

    public BigDecimal getBestAsk() {
        return this.bestAsk;
    }

    public List<QuotePriceHistory> getPriceHistory() {
        return this.priceHistory;
    }

    public BigDecimal getRate() {
        return this.rate;
    }
}