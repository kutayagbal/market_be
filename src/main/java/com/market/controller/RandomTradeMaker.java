package com.market.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.transaction.Transactional;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.market.common.MarketException;
import com.market.entity.Stock;
import com.market.entity.User;
import com.market.entity.UserStock;
import com.market.model.Order;
import com.market.repo.StockRepo;
import com.market.repo.UserRepo;
import com.market.service.OrderService;
import com.market.websocket.Handler;

@RestController
@RequestMapping("/make")
@CrossOrigin
public class RandomTradeMaker {
    private final OrderService orderService;
    private final UserRepo userRepo;
    private final StockRepo stockRepo;
    private final Handler webSocketHandler;
    private Random random;

    public RandomTradeMaker(OrderService orderService, UserRepo userRepo, StockRepo stockRepo,
            Handler webSocketHandler) {
        this.orderService = orderService;
        this.userRepo = userRepo;
        this.stockRepo = stockRepo;
        this.webSocketHandler = webSocketHandler;
        this.random = new Random();

    }

    @GetMapping("/buy")
    public void buy(@RequestParam BigDecimal price, @RequestParam Integer quantity,
            @RequestParam(required = false) String stockName) {
        Stock stock;
        if (!StringUtils.hasText(stockName)) {
            stock = selectRandomStock();
        } else {
            Optional<Stock> stockOpt = stockRepo.findByName(stockName);

            if (stockOpt.isPresent()) {
                stock = stockOpt.get();
            } else {
                throw new MarketException("Stock can not be found!");
            }
        }

        String consumer = createUserWithMoney();
        Order demand = new Order(stock.getName(), quantity, price);

        Stock bought = orderService.consume(demand, consumer);
        if (bought != null) {
            webSocketHandler.onTrade(bought);
        }
    }

    @GetMapping("/sell")
    public void sell(@RequestParam BigDecimal price, @RequestParam Integer quantity,
            @RequestParam(required = false) String stockName) {
        Stock stock;
        if (!StringUtils.hasText(stockName)) {
            stock = selectRandomStock();
        } else {
            Optional<Stock> stockOpt = stockRepo.findByName(stockName);

            if (stockOpt.isPresent()) {
                stock = stockOpt.get();
            } else {
                throw new MarketException("Stock can not be found!");
            }
        }

        String supplier = createUserWithStock(stock);
        Order supply = new Order(stock.getName(), quantity, price);

        Stock sold = orderService.supply(supply, supplier);
        if (sold != null) {
            webSocketHandler.onTrade(sold);
        }
    }

    private Stock selectRandomStock() {
        List<Stock> stocks = stockRepo.findAll();
        return stocks.get(random.nextInt(stocks.size()));
    }

    @Transactional
    public String createUserWithMoney() {
        return createRandomUser(new BigDecimal("1000000"), new ArrayList());
    }

    @Transactional
    public String createUserWithStock(Stock stock) {
        return createRandomUser(BigDecimal.ZERO, List.of(createUserStock(stock, 100)));
    }

    private UserStock createUserStock(Stock stock, Integer quantity) {
        return new UserStock(stock, quantity);
    }

    private String createRandomUser(BigDecimal balance, List<UserStock> stocks) {
        String username = createRandomUsername();
        userRepo.save(
                new User(username, "password", "Full Name Jr", balance, new ArrayList(), new ArrayList(), stocks));
        return username;
    }

    private String createRandomUsername() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append((char) (random.nextInt(26) + 'a'));
        }
        return sb.toString();
    }
}
