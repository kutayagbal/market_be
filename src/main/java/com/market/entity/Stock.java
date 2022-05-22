package com.market.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal rate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "stock")
    @OrderBy("time ASC")
    private List<PriceHistory> priceHistory;

    public Stock(String name, BigDecimal price, BigDecimal bestBid, BigDecimal bestAsk, BigDecimal rate,
            List<PriceHistory> priceHistory) {
        this.name = name;
        this.price = price;
        this.bestAsk = bestAsk;
        this.bestBid = bestBid;
        this.rate = rate;
        this.priceHistory = priceHistory;
    }

    public Stock() {
        super();
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getBestBid() {
        return bestBid;
    }

    public BigDecimal getBestAsk() {
        return bestAsk;
    }

    public List<PriceHistory> getPriceHistory() {
        return this.priceHistory;
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public void setPrice(BigDecimal price) {
        setRate(price);
        this.price = price;
    }

    public void setBestBid(BigDecimal bestBid) {
        this.bestBid = bestBid;
    }

    public void setBestAsk(BigDecimal bestAsk) {
        this.bestAsk = bestAsk;
    }

    private void setRate(BigDecimal newPrice) {
        if (this.price.compareTo(BigDecimal.ZERO) != 0) {
            this.rate = newPrice.multiply(this.rate.add(BigDecimal.valueOf(100))).divide(this.price)
                    .subtract(BigDecimal.valueOf(100));
        } else {
            this.rate = BigDecimal.ZERO;
        }
    }
}
