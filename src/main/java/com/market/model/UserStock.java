package com.market.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class UserStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock")
    private Stock stock;

    public UserStock(Stock stock, Integer quantity) {
        this.stock = stock;
        this.quantity = quantity;
    }

    public UserStock() {
        super();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Stock getStock() {
        return stock;
    }

    public void sell(Integer quantity) {
        this.quantity -= quantity;
    }

    public void buy(Integer quantity) {
        this.quantity += quantity;
    }
}
