package com.market.model;

import java.math.BigDecimal;

public class Order {
	private String stock;
	private Integer quantity;
	private BigDecimal price;

	public Order(String stock, Integer quantity, BigDecimal price) {
		super();
		this.stock = stock;
		this.quantity = quantity;
		this.price = price;
	}

	public String getStock() {
		return stock;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setStock(String stock) {
		this.stock = stock;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Order [price=" + price + ", quantity=" + quantity + ", stock=" + stock + "]";
	}

}
