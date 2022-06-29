package com.market.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.market.common.Constants;
import com.market.common.MarketException;
import com.market.entity.Stock;
import com.market.model.Order;
import com.market.service.OrderService;
import com.market.websocket.Handler;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final Handler webSocketHandler;

    public OrderController(OrderService orderService, Handler webSocketHandler) {
        this.orderService = orderService;
        this.webSocketHandler = webSocketHandler;
    }

    @GetMapping("/demands")
    public List<Order> getUserDemands(@RequestParam String username) {
        validateUsername(username);
        return orderService.getDemands(username).stream()
                .map(demand -> new Order(demand.getStock().getName(), demand.getQuantity(),
                        demand.getPrice()))
                .collect(Collectors.toList());
    }

    @GetMapping("/supplies")
    public List<Order> getUserSupplies(@RequestParam String username) {
        validateUsername(username);
        return orderService.getSupplies(username).stream()
                .map(supply -> new Order(supply.getStock().getName(), supply.getQuantity(),
                        supply.getPrice()))
                .collect(Collectors.toList());
    }

    @PostMapping("/consume")
    public void consume(@RequestBody Order demand, @RequestParam String username) {
        validateUsername(username);
        validateOrder(demand);

        Stock stock = orderService.consume(demand, username);
        if (stock != null) {
            webSocketHandler.onTrade(stock);
        }
    }

    @PostMapping("/supply")
    public void supply(@RequestBody Order supply, @RequestParam String username) {
        validateUsername(username);
        validateOrder(supply);
        Stock stock = orderService.supply(supply, username);
        if (stock != null) {
            webSocketHandler.onTrade(stock);
        }
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new MarketException(Constants.NO_USR_MSG);
        }
    }

    private void validateOrder(Order order) {
        if (order.getQuantity().compareTo(0) <= 0) {
            throw new MarketException(Constants.INV_QUANTITY_MSG);
        }

        if (order.getPrice() != null && order.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new MarketException(Constants.INV_PRICE_MSG);
        }
    }
}
