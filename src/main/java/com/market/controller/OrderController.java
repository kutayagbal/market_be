package com.market.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.market.common.Constants;
import com.market.common.MarketException;
import com.market.model.Order;
import com.market.service.OrderService;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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
        orderService.consume(demand, username);
    }

    @PostMapping("/supply")
    public void supply(@RequestBody Order supply, @RequestParam String username) {
        validateUsername(username);
        validateOrder(supply);
        orderService.supply(supply, username);
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
    }
}
