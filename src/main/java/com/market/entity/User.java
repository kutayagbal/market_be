package com.market.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import com.market.common.MarketCheckedException;
import com.market.model.Order;

@Entity
public class User {
	@Id
	@GeneratedValue
	private Long id;

	private String username;
	private String password;
	private String fullName;

	private BigDecimal balance;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "consumer")
	private List<Demand> demands;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "supplier")
	private List<Supply> supplies;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "user")
	private List<UserStock> userStocks;

	public User(String username, String password, String fullName, BigDecimal balance,
			List<Demand> demands, List<Supply> supplies, List<UserStock> userStocks) {
		super();
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.balance = balance;
		this.demands = demands;
		this.supplies = supplies;
		this.userStocks = userStocks;
	}

	public User() {
		super();
	}

	public Long getId() {
		return id;
	}

	public String getUserName() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFullName() {
		return fullName;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public List<Demand> getDemands() {
		return demands;
	}

	public List<Supply> getSupplies() {
		return supplies;
	}

	public List<UserStock> getUserStocks() {
		return userStocks;
	}

	@Override
	public String toString() {
		return "User [balance=" + balance + ", fullName=" + fullName + ", username=" + username + "]";
	}

	public void sellStock(Order supply) throws MarketCheckedException {
		Optional<UserStock> userStockOpt = this.userStocks.stream()
				.filter(st -> st.getStock().getName().equals(supply.getStock()))
				.findFirst();

		if (userStockOpt.isPresent()) {
			BigDecimal totalPrice = supply.getPrice().multiply(BigDecimal.valueOf(supply.getQuantity()));
			UserStock userStock = userStockOpt.get();

			if (userStock.getQuantity().compareTo(supply.getQuantity()) == 0) {
				this.userStocks.remove(userStock);
			} else if (userStock.getQuantity().compareTo(supply.getQuantity()) > 0) {
				userStock.sell(supply.getQuantity());
			} else {
				throw new MarketCheckedException("Not enough stock to sell!");
			}

			balance = balance.add(totalPrice);
		} else {
			throw new MarketCheckedException("No stock to sell!");
		}
	}

	public void buyStock(Order demand, Stock stock) throws MarketCheckedException {
		BigDecimal totalPrice = demand.getPrice().multiply(BigDecimal.valueOf(demand.getQuantity()));

		if (balance.compareTo(totalPrice) >= 0) {
			Optional<UserStock> userStockOpt = userStocks.stream()
					.filter(st -> st.getStock().getName().equals(demand.getStock()))
					.findFirst();

			if (userStockOpt.isPresent()) {
				userStockOpt.get().buy(demand.getQuantity());
			} else {
				userStocks.add(new UserStock(stock, demand.getQuantity()));
			}

			balance = balance.subtract(totalPrice);
		} else {
			throw new MarketCheckedException("Not enough balance to buy!");
		}
	}
}
