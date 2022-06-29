package com.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.market.entity.Demand;
import com.market.entity.Stock;
import com.market.entity.Supply;
import com.market.entity.Trade;
import com.market.entity.User;
import com.market.entity.UserStock;
import com.market.model.Order;
import com.market.repo.DemandRepo;
import com.market.repo.StockRepo;
import com.market.repo.SupplyRepo;
import com.market.repo.TradeRepo;
import com.market.repo.UserRepo;

public class OrderServiceTest {

        private OrderService orderService;

        private DemandRepo mockDemandRepo = Mockito.mock(DemandRepo.class);

        private SupplyRepo mockSupplyRepo = Mockito.mock(SupplyRepo.class);

        private UserRepo mockUserRepo = Mockito.mock(UserRepo.class);

        private StockRepo mockStockRepo = Mockito.mock(StockRepo.class);

        private TradeRepo mockTradeRepo = Mockito.mock(TradeRepo.class);

        @Test
        void testSellWithoutMatch() {
                String mockSupplierUsername = "mockSupplier";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance = BigDecimal.valueOf(10);
                Integer mockSupplyQuantity = 2;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(3);

                List<UserStock> mockSupplierStocks = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier = new User(mockSupplierUsername, "mockPassword", "mockFullName", mockSupplierBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks);

                Order mockSupplyOrder = new Order(mockStockName, mockSupplyQuantity, null);

                when(mockUserRepo.findByUsername(mockSupplierUsername)).thenReturn(Optional.of(mockSupplier));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.supply(mockSupplyOrder, mockSupplierUsername);

                // check supplier supplies
                List<Supply> supplies = mockSupplier.getSupplies();
                assertNotNull(supplies);
                assertTrue(supplies.size() > 0);

                Supply supply = supplies.get(0);

                assertEquals(mockStockName, supply.getStock().getName());
                assertEquals(mockSupplyQuantity, supply.getQuantity());
                assertEquals(mockSupplyPrice, supply.getPrice());

                // check supplier demands
                assertEquals(0, mockSupplier.getDemands().size());

                // check supplier stocks
                assertEquals(mockSupplierStocks, mockSupplier.getUserStocks());

                // check supplier balance
                assertEquals(mockSupplierBalance, mockSupplier.getBalance());

                // check trade
                verify(mockTradeRepo, times(0)).save(any(Trade.class));
        }

        @Test
        void testSellWithExactMatch() {
                String mockSupplierUsername = "mockSupplier";
                String mockConsumerUsername = "mockConsumer";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance = BigDecimal.valueOf(10);
                BigDecimal mockConsumerBalance = BigDecimal.valueOf(20);
                Integer mockSupplyQuantity = 2;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(2);
                BigDecimal mockDemandPrice = BigDecimal.valueOf(3);

                List<UserStock> mockSupplierStocks = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier = new User(mockSupplierUsername, "mockPassword", "mockFullName", mockSupplierBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks);

                User mockConsumer = new User(mockConsumerUsername, "mockPassword", "mockFullName", mockConsumerBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockSupplyOrder = new Order(mockStockName, mockSupplyQuantity, null);

                Demand mockDemand = new Demand(mockStock, mockSupplyQuantity, mockDemandPrice, mockConsumer);
                when(mockUserRepo.findByUsername(mockSupplierUsername)).thenReturn(Optional.of(mockSupplier));
                when(mockUserRepo.findByUsername(mockConsumerUsername)).thenReturn(Optional.of(mockConsumer));
                when(mockDemandRepo.findAllByStockOrderByPriceDesc(mockStock)).thenReturn(List.of(mockDemand));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.supply(mockSupplyOrder, mockSupplierUsername);

                // check supplier supplies
                assertEquals(0, mockSupplier.getSupplies().size());

                // check consumer supplies
                assertEquals(0, mockConsumer.getSupplies().size());

                // check supplier demands
                assertEquals(0, mockSupplier.getDemands().size());

                // check consumer demands
                assertEquals(0, mockConsumer.getDemands().size());

                // check supplier stocks
                assertEquals(0, mockSupplier.getUserStocks().size());

                // check consumer stocks
                assertEquals(1, mockConsumer.getUserStocks().size());
                UserStock userStock = mockConsumer.getUserStocks().get(0);
                assertEquals(mockStock, userStock.getStock());
                assertEquals(mockSupplyQuantity, userStock.getQuantity());

                BigDecimal totalPrice = mockDemandPrice.multiply(BigDecimal.valueOf(mockSupplyQuantity));

                // check supplier balance
                assertEquals(mockSupplierBalance.add(totalPrice), mockSupplier.getBalance());

                // check consumer balance
                assertEquals(mockConsumerBalance.subtract(totalPrice), mockConsumer.getBalance());

                // check trade
                ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
                verify(mockTradeRepo, times(1)).save(argumentCaptor.capture());

                Trade trade = argumentCaptor.getValue();
                assertEquals(mockStock, trade.getStock());
                assertEquals(mockDemandPrice, trade.getPrice());
                assertEquals(mockSupplyQuantity, trade.getQuantity());
                assertEquals(mockSupplier, trade.getSupplier());
                assertEquals(mockConsumer, trade.getConsumer());
        }

        @Test
        void testBuyWithoutMatch() {
                String mockConsumerUsername = "mockConsumer";
                String mockStockName = "mockStockSupply";
                BigDecimal mockConsumerBalance = BigDecimal.valueOf(20);
                Integer mockSupplyQuantity = 3;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(5);

                User mockConsumer = new User(mockConsumerUsername, "mockPassword", "mockFullName", mockConsumerBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockDemandOrder = new Order(mockStockName, mockSupplyQuantity, null);
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());

                when(mockUserRepo.findByUsername(mockConsumerUsername)).thenReturn(Optional.of(mockConsumer));
                when(mockStockRepo.findByName(mockStockName)).thenReturn(Optional.of(mockStock));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.consume(mockDemandOrder, mockConsumerUsername);

                // check consumer supplies
                assertEquals(0, mockConsumer.getSupplies().size());

                // check consumer demands
                List<Demand> demands = mockConsumer.getDemands();
                assertNotNull(demands);
                assertTrue(demands.size() > 0);

                Demand demand = demands.get(0);

                assertEquals(mockStockName, demand.getStock().getName());
                assertEquals(mockSupplyQuantity, demand.getQuantity());
                assertEquals(mockSupplyPrice, demand.getPrice());

                // check consumer stocks
                assertEquals(0, mockConsumer.getUserStocks().size());

                // check consumer balance
                assertEquals(mockConsumerBalance, mockConsumer.getBalance());

                // check trade
                verify(mockTradeRepo, times(0)).save(any(Trade.class));
        }

        @Test
        void testBuyWithExactMatch() {
                String mockSupplierUsername = "mockSupplier";
                String mockConsumerUsername = "mockConsumer";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance = BigDecimal.valueOf(10);
                BigDecimal mockConsumerBalance = BigDecimal.valueOf(20);
                Integer mockSupplyQuantity = 2;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(2);

                List<UserStock> mockSupplierStocks = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier = new User(mockSupplierUsername, "mockPassword", "mockFullName", mockSupplierBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks);

                User mockConsumer = new User(mockConsumerUsername, "mockPassword", "mockFullName", mockConsumerBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockDemandOrder = new Order(mockStockName, mockSupplyQuantity, null);

                Supply mockSupply = new Supply(mockStock, mockSupplyQuantity, mockSupplyPrice, mockSupplier);
                when(mockUserRepo.findByUsername(mockSupplierUsername)).thenReturn(Optional.of(mockSupplier));
                when(mockUserRepo.findByUsername(mockConsumerUsername)).thenReturn(Optional.of(mockConsumer));
                when(mockStockRepo.findByName(mockStockName)).thenReturn(Optional.of(mockStock));
                when(mockSupplyRepo.findAllByStockOrderByPriceAsc(mockStock)).thenReturn(List.of(mockSupply));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.consume(mockDemandOrder, mockConsumerUsername);

                // check supplier supplies
                assertEquals(0, mockSupplier.getSupplies().size());

                // check consumer supplies
                assertEquals(0, mockConsumer.getSupplies().size());

                // check supplier demands
                assertEquals(0, mockSupplier.getDemands().size());

                // check consumer demands
                assertEquals(0, mockConsumer.getDemands().size());

                // check supplier stocks
                assertEquals(0, mockSupplier.getUserStocks().size());

                // check consumer stocks
                assertEquals(1, mockConsumer.getUserStocks().size());
                UserStock userStock = mockConsumer.getUserStocks().get(0);
                assertEquals(mockStock, userStock.getStock());
                assertEquals(mockSupplyQuantity, userStock.getQuantity());

                BigDecimal totalPrice = mockSupplyPrice.multiply(BigDecimal.valueOf(mockSupplyQuantity));

                // check supplier balance
                assertEquals(mockSupplierBalance.add(totalPrice), mockSupplier.getBalance());

                // check consumer balance
                assertEquals(mockConsumerBalance.subtract(totalPrice), mockConsumer.getBalance());

                // check trade
                ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
                verify(mockTradeRepo, times(1)).save(argumentCaptor.capture());

                Trade trade = argumentCaptor.getValue();
                assertEquals(mockStock, trade.getStock());
                assertEquals(mockSupplyPrice, trade.getPrice());
                assertEquals(mockSupplyQuantity, trade.getQuantity());
                assertEquals(mockSupplier, trade.getSupplier());
                assertEquals(mockConsumer, trade.getConsumer());
        }

        @Test
        void testSellWithPartialMatch() {
                String mockSupplierUsername = "mockSupplier";
                String mockConsumerUsername = "mockConsumer";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance = BigDecimal.valueOf(10);
                BigDecimal mockConsumerBalance = BigDecimal.valueOf(10);
                Integer mockSupplyQuantity = 9;
                Integer mockDemandQuantity = 3;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(2);
                BigDecimal mockDemandPrice = BigDecimal.valueOf(3);

                List<UserStock> mockSupplierStocks = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier = new User(mockSupplierUsername, "mockPassword", "mockFullName", mockSupplierBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks);

                User mockConsumer = new User(mockConsumerUsername, "mockPassword", "mockFullName", mockConsumerBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockSupplyOrder = new Order(mockStockName, mockSupplyQuantity, null);

                Demand mockDemand = new Demand(mockStock, mockDemandQuantity, mockDemandPrice, mockConsumer);
                when(mockUserRepo.findByUsername(mockSupplierUsername)).thenReturn(Optional.of(mockSupplier));
                when(mockUserRepo.findByUsername(mockConsumerUsername)).thenReturn(Optional.of(mockConsumer));
                when(mockDemandRepo.findAllByStockOrderByPriceDesc(mockStock)).thenReturn(List.of(mockDemand));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.supply(mockSupplyOrder, mockSupplierUsername);

                // check supplier supplies
                List<Supply> supplies = mockSupplier.getSupplies();
                assertEquals(1, supplies.size());

                Supply supply = supplies.get(0);
                assertEquals(mockStockName, supply.getStock().getName());
                assertEquals(mockSupplyQuantity - mockDemandQuantity, supply.getQuantity());
                assertEquals(mockDemandPrice, supply.getPrice());

                // check consumer supplies
                assertEquals(0, mockConsumer.getSupplies().size());

                // check supplier demands
                assertEquals(0, mockSupplier.getDemands().size());

                // check consumer demands
                assertEquals(0, mockConsumer.getDemands().size());

                // check supplier stocks
                assertEquals(1, mockSupplier.getUserStocks().size());
                UserStock supplierStock = mockSupplier.getUserStocks().get(0);
                assertEquals(mockStock, supplierStock.getStock());
                assertEquals(mockSupplyQuantity - mockDemandQuantity, supplierStock.getQuantity());

                BigDecimal totalPrice = mockDemandPrice.multiply(BigDecimal.valueOf(mockDemandQuantity));

                // check consumer stocks
                assertEquals(1, mockConsumer.getUserStocks().size());
                UserStock consumerStock = mockConsumer.getUserStocks().get(0);
                assertEquals(mockStock, consumerStock.getStock());
                assertEquals(mockDemandQuantity, consumerStock.getQuantity());

                // check supplier balance
                assertEquals(mockSupplierBalance.add(totalPrice), mockSupplier.getBalance());

                // check consumer balance
                assertEquals(mockConsumerBalance.subtract(totalPrice), mockConsumer.getBalance());

                // check trade
                ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
                verify(mockTradeRepo, times(1)).save(argumentCaptor.capture());

                Trade trade = argumentCaptor.getValue();
                assertEquals(mockStock, trade.getStock());
                assertEquals(mockDemandPrice, trade.getPrice());
                assertEquals(mockDemandQuantity, trade.getQuantity());
                assertEquals(mockSupplier, trade.getSupplier());
                assertEquals(mockConsumer, trade.getConsumer());
        }

        @Test
        void testBuyWithPartialMatch() {
                String mockSupplierUsername = "mockSupplier";
                String mockConsumerUsername = "mockConsumer";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance = BigDecimal.valueOf(10);
                BigDecimal mockConsumerBalance = BigDecimal.valueOf(20);
                Integer mockSupplyQuantity = 2;
                Integer mockDemandQuantity = 9;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(2);

                List<UserStock> mockSupplierStocks = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier = new User(mockSupplierUsername, "mockPassword0", "mockFullName0",
                                mockSupplierBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks);

                User mockConsumer = new User(mockConsumerUsername, "mockPassword1", "mockFullName1",
                                mockConsumerBalance,
                                new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockDemandOrder = new Order(mockStockName, mockDemandQuantity, null);

                Supply mockSupply = new Supply(mockStock, mockSupplyQuantity, mockSupplyPrice, mockSupplier);
                when(mockUserRepo.findByUsername(mockSupplierUsername)).thenReturn(Optional.of(mockSupplier));
                when(mockUserRepo.findByUsername(mockConsumerUsername)).thenReturn(Optional.of(mockConsumer));
                when(mockStockRepo.findByName(mockStockName)).thenReturn(Optional.of(mockStock));
                when(mockSupplyRepo.findAllByStockOrderByPriceAsc(mockStock)).thenReturn(List.of(mockSupply));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.consume(mockDemandOrder, mockConsumerUsername);

                // check supplier supplies
                assertEquals(0, mockSupplier.getSupplies().size());

                // check consumer supplies
                assertEquals(0, mockConsumer.getSupplies().size());

                // check supplier demands
                assertEquals(0, mockSupplier.getDemands().size());

                // check consumer demands
                List<Demand> demands = mockConsumer.getDemands();
                assertEquals(1, demands.size());

                Demand demand = demands.get(0);
                assertEquals(mockStockName, demand.getStock().getName());
                assertEquals(mockDemandQuantity - mockSupplyQuantity, demand.getQuantity());
                assertEquals(mockSupplyPrice, demand.getPrice());

                // check supplier stocks
                assertEquals(0, mockSupplier.getUserStocks().size());

                // check consumer stocks
                assertEquals(1, mockConsumer.getUserStocks().size());
                UserStock userStock = mockConsumer.getUserStocks().get(0);
                assertEquals(mockStock, userStock.getStock());
                assertEquals(mockSupplyQuantity, userStock.getQuantity());

                BigDecimal totalPrice = mockSupplyPrice.multiply(BigDecimal.valueOf(mockSupplyQuantity));

                // check supplier balance
                assertEquals(mockSupplierBalance.add(totalPrice), mockSupplier.getBalance());

                // check consumer balance
                assertEquals(mockConsumerBalance.subtract(totalPrice), mockConsumer.getBalance());

                // check trade
                ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
                verify(mockTradeRepo, times(1)).save(argumentCaptor.capture());

                Trade trade = argumentCaptor.getValue();
                assertEquals(mockStock, trade.getStock());
                assertEquals(mockSupplyPrice, trade.getPrice());
                assertEquals(mockSupplyQuantity, trade.getQuantity());
                assertEquals(mockSupplier, trade.getSupplier());
                assertEquals(mockConsumer, trade.getConsumer());
        }

}
