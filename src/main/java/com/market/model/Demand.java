package com.market.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Demand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock")
    private Stock stock;

    public Demand(Stock stock, Integer quantity, BigDecimal price, User user) {
        super();
        this.stock = stock;
        this.quantity = quantity;
        this.price = price;
        this.user = user;
    }

    public Demand() {
        super();
    }

    public Long getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public User getUser() {
        return user;
    }
}
