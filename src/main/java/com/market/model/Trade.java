package com.market.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class Trade {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer quantity;
	private BigDecimal price;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock")
	private Stock stock;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "consumer")
	private User consumer;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supplier")
	private User supplier;

	public Trade(Stock stock, Integer quantity, BigDecimal price, User consumer, User supplier) {
		super();
		this.stock = stock;
		this.quantity = quantity;
		this.price = price;
		this.consumer = consumer;
		this.supplier = supplier;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Stock getStock() {
		return stock;
	}

	public User getConsumer() {
		return consumer;
	}

	public User getSupplier() {
		return supplier;
	}
}
