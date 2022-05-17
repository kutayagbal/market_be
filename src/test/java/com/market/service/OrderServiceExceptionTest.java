package com.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.market.common.Constants;
import com.market.model.Demand;
import com.market.model.Order;
import com.market.model.Stock;
import com.market.model.Supply;
import com.market.model.Trade;
import com.market.model.User;
import com.market.model.UserStock;
import com.market.repo.DemandRepo;
import com.market.repo.StockRepo;
import com.market.repo.SupplyRepo;
import com.market.repo.TradeRepo;
import com.market.repo.UserRepo;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class OrderServiceExceptionTest {

        private OrderService orderService;

        private DemandRepo mockDemandRepo = Mockito.mock(DemandRepo.class);

        private SupplyRepo mockSupplyRepo = Mockito.mock(SupplyRepo.class);

        private UserRepo mockUserRepo = Mockito.mock(UserRepo.class);

        private StockRepo mockStockRepo = Mockito.mock(StockRepo.class);

        private TradeRepo mockTradeRepo = Mockito.mock(TradeRepo.class);

        @Test
        void testSellWithExactMatch_ThoughConsumerCheckedException() {
                String mockSupplierUsername = "mockSupplier";
                String mockConsumerUsername0 = "mockConsumer0";
                String mockConsumerUsername1 = "mockConsumer1";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance = BigDecimal.valueOf(10);
                BigDecimal mockConsumerBalance0 = BigDecimal.valueOf(1);
                BigDecimal mockConsumerBalance1 = BigDecimal.valueOf(20);
                Integer mockSupplyQuantity = 2;
                BigDecimal mockSupplyPrice = BigDecimal.valueOf(2);
                BigDecimal mockDemandPrice0 = BigDecimal.valueOf(4);
                BigDecimal mockDemandPrice1 = BigDecimal.valueOf(3);

                List<UserStock> mockSupplierStocks = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier = new User(mockSupplierUsername, "mockPassword", "mockFullName", mockSupplierBalance,
                                Constants.Roles.SCOPE_TRADER.name(), new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks);

                User mockConsumer0 = new User(mockConsumerUsername0, "mockPassword0", "mockFullName0",
                                mockConsumerBalance0,
                                Constants.Roles.SCOPE_TRADER.name(), new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                User mockConsumer1 = new User(mockConsumerUsername1, "mockPassword1", "mockFullName1",
                                mockConsumerBalance1,
                                Constants.Roles.SCOPE_TRADER.name(), new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockSupplyOrder = new Order(mockStockName, mockSupplyQuantity, null);

                Demand mockDemand0 = new Demand(mockStock, mockSupplyQuantity, mockDemandPrice0, mockConsumer0);
                Demand mockDemand1 = new Demand(mockStock, mockSupplyQuantity, mockDemandPrice1, mockConsumer1);
                when(mockUserRepo.findByUsername(mockSupplierUsername)).thenReturn(Optional.of(mockSupplier));
                when(mockUserRepo.findByUsername(mockConsumerUsername0)).thenReturn(Optional.of(mockConsumer0));
                when(mockUserRepo.findByUsername(mockConsumerUsername1)).thenReturn(Optional.of(mockConsumer1));
                when(mockDemandRepo.findAllByStockOrderByPriceDesc(mockStock))
                                .thenReturn(List.of(mockDemand0, mockDemand1));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.supply(mockSupplyOrder, mockSupplierUsername);

                // check unsuccessful consumer
                assertEquals(0, mockConsumer0.getSupplies().size());
                assertEquals(0, mockConsumer0.getDemands().size());
                assertEquals(0, mockConsumer0.getUserStocks().size());
                assertEquals(mockConsumerBalance0, mockConsumer0.getBalance());

                // check supplier supplies
                assertEquals(0, mockSupplier.getSupplies().size());

                // check consumer supplies
                assertEquals(0, mockConsumer1.getSupplies().size());

                // check supplier demands
                assertEquals(0, mockSupplier.getDemands().size());

                // check consumer demands
                assertEquals(0, mockConsumer1.getDemands().size());

                // check supplier stocks
                assertEquals(0, mockSupplier.getUserStocks().size());

                // check consumer stocks
                assertEquals(1, mockConsumer1.getUserStocks().size());
                UserStock userStock = mockConsumer1.getUserStocks().get(0);
                assertEquals(mockStock, userStock.getStock());
                assertEquals(mockSupplyQuantity, userStock.getQuantity());

                BigDecimal totalPrice = mockDemandPrice1.multiply(BigDecimal.valueOf(mockSupplyQuantity));

                // check supplier balance
                assertEquals(mockSupplierBalance.add(totalPrice), mockSupplier.getBalance());

                // check consumer balance
                assertEquals(mockConsumerBalance1.subtract(totalPrice), mockConsumer1.getBalance());

                // check trade
                ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
                verify(mockTradeRepo, times(1)).save(argumentCaptor.capture());

                Trade trade = argumentCaptor.getValue();
                assertEquals(mockStock, trade.getStock());
                assertEquals(mockDemandPrice1, trade.getPrice());
                assertEquals(mockSupplyQuantity, trade.getQuantity());
                assertEquals(mockSupplier, trade.getSupplier());
                assertEquals(mockConsumer1, trade.getConsumer());
        }

        @Test
        void testBuyWithExactMatch_ThoughSupplierCheckedException() {
                String mockSupplierUsername0 = "mockSupplier0";
                String mockSupplierUsername1 = "mockSupplier1";
                String mockConsumerUsername = "mockConsumer";
                String mockStockName = "mockStockSupply";
                BigDecimal mockSupplierBalance0 = BigDecimal.valueOf(5);
                BigDecimal mockSupplierBalance1 = BigDecimal.valueOf(10);
                BigDecimal mockConsumerBalance = BigDecimal.valueOf(20);
                Integer mockSupplyQuantity = 2;
                BigDecimal mockSupplyPrice0 = BigDecimal.valueOf(2);
                BigDecimal mockSupplyPrice1 = BigDecimal.valueOf(3);

                List<UserStock> mockSupplierStocks1 = new ArrayList<>();
                Stock mockStock = new Stock(mockStockName, mockSupplyPrice1, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, new ArrayList<>());
                mockSupplierStocks1.add(new UserStock(mockStock, mockSupplyQuantity));

                User mockSupplier0 = new User(mockSupplierUsername0, "mockPassword0", "mockFullName0",
                                mockSupplierBalance0,
                                Constants.Roles.SCOPE_TRADER.name(), new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                User mockSupplier1 = new User(mockSupplierUsername1, "mockPassword1", "mockFullName1",
                                mockSupplierBalance1,
                                Constants.Roles.SCOPE_TRADER.name(), new ArrayList<Demand>(), new ArrayList<Supply>(),
                                mockSupplierStocks1);

                User mockConsumer = new User(mockConsumerUsername, "mockPassword", "mockFullName", mockConsumerBalance,
                                Constants.Roles.SCOPE_TRADER.name(), new ArrayList<Demand>(), new ArrayList<Supply>(),
                                new ArrayList<UserStock>());

                Order mockDemandOrder = new Order(mockStockName, mockSupplyQuantity, null);

                Supply mockSupply0 = new Supply(mockStock, mockSupplyQuantity, mockSupplyPrice0, mockSupplier0);
                Supply mockSupply1 = new Supply(mockStock, mockSupplyQuantity, mockSupplyPrice1, mockSupplier1);
                when(mockUserRepo.findByUsername(mockSupplierUsername0)).thenReturn(Optional.of(mockSupplier0));
                when(mockUserRepo.findByUsername(mockSupplierUsername1)).thenReturn(Optional.of(mockSupplier1));
                when(mockUserRepo.findByUsername(mockConsumerUsername)).thenReturn(Optional.of(mockConsumer));
                when(mockStockRepo.findByName(mockStockName)).thenReturn(Optional.of(mockStock));
                when(mockSupplyRepo.findAllByStockOrderByPriceAsc(mockStockName))
                                .thenReturn(List.of(mockSupply0, mockSupply1));

                orderService = new OrderService(mockUserRepo, mockDemandRepo, mockSupplyRepo, mockStockRepo,
                                mockTradeRepo);
                orderService.consume(mockDemandOrder, mockConsumerUsername);

                // check unsuccessful supplier
                assertEquals(0, mockSupplier0.getSupplies().size());
                assertEquals(0, mockSupplier0.getDemands().size());
                assertEquals(0, mockSupplier0.getUserStocks().size());
                assertEquals(mockSupplierBalance0, mockSupplier0.getBalance());

                // check supplier supplies
                assertEquals(0, mockSupplier1.getSupplies().size());

                // check consumer supplies
                assertEquals(0, mockConsumer.getSupplies().size());

                // check supplier demands
                assertEquals(0, mockSupplier1.getDemands().size());

                // check consumer demands
                assertEquals(0, mockConsumer.getDemands().size());

                // check supplier stocks
                assertEquals(0, mockSupplier1.getUserStocks().size());

                // check consumer stocks
                assertEquals(1, mockConsumer.getUserStocks().size());
                UserStock userStock = mockConsumer.getUserStocks().get(0);
                assertEquals(mockStock, userStock.getStock());
                assertEquals(mockSupplyQuantity, userStock.getQuantity());

                BigDecimal totalPrice = mockSupplyPrice1.multiply(BigDecimal.valueOf(mockSupplyQuantity));

                // check supplier balance
                assertEquals(mockSupplierBalance1.add(totalPrice), mockSupplier1.getBalance());

                // check consumer balance
                assertEquals(mockConsumerBalance.subtract(totalPrice), mockConsumer.getBalance());

                // check trade
                ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
                verify(mockTradeRepo, times(1)).save(argumentCaptor.capture());

                Trade trade = argumentCaptor.getValue();
                assertEquals(mockStock, trade.getStock());
                assertEquals(mockSupplyPrice1, trade.getPrice());
                assertEquals(mockSupplyQuantity, trade.getQuantity());
                assertEquals(mockSupplier1, trade.getSupplier());
                assertEquals(mockConsumer, trade.getConsumer());
        }

}
