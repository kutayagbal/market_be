package com.market.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime time;
    private BigDecimal price;

    public PriceHistory(LocalDateTime time, BigDecimal price) {
        this.time = time;
        this.price = price;
    }

    public PriceHistory() {
        super();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
