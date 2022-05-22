package com.market.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.market.common.Constants;
import com.market.common.MarketCheckedException;
import com.market.common.MarketException;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private static final Log logger = LogFactory.getLog(OrderService.class);
    private final DemandRepo demandRepo;
    private final SupplyRepo supplyRepo;
    private final UserRepo userRepo;
    private final TradeRepo tradeRepo;
    private final StockRepo stockRepo;

    public OrderService(UserRepo userRepo, DemandRepo demandRepo,
            SupplyRepo supplyRepo, StockRepo stockRepo,
            TradeRepo tradeRepo) {
        super();
        this.userRepo = userRepo;
        this.stockRepo = stockRepo;
        this.demandRepo = demandRepo;
        this.supplyRepo = supplyRepo;
        this.tradeRepo = tradeRepo;
    }

    @Transactional
    public void supply(Order supply, String username) {
        Optional<User> supplierOpt = userRepo.findByUsername(username);

        if (supplierOpt.isPresent()) {
            User supplier = supplierOpt.get();
            Stock stock = checkStock(supply, supplier);
            tradeSupply(stock, supply, supplier);
        } else {
            throw new MarketException(Constants.NO_USR_MSG);
        }
    }

    private void tradeSupply(Stock stock, Order supply, User supplier) {
        List<Demand> demands = demandRepo.findAllByStockOrderByPriceDesc(stock);

        if (!demands.isEmpty()) {
            BigDecimal lastDemandPrice = matchSupply(demands, supply, supplier, stock);

            if (lastDemandPrice != null && BigDecimal.valueOf(-1).compareTo(lastDemandPrice) != 0) {
                supplier.getSupplies().add(new Supply(stock, supply.getQuantity(), lastDemandPrice, supplier));
                if (stock.getBestAsk().compareTo(lastDemandPrice) > 0) {
                    stock.setBestAsk(lastDemandPrice);
                }
            } else if (lastDemandPrice == null) {
                // last trade price
                supplier.getSupplies().add(new Supply(stock, supply.getQuantity(), stock.getPrice(), supplier));
                if (stock.getBestAsk().compareTo(stock.getPrice()) > 0) {
                    stock.setBestAsk(stock.getPrice());
                }
            }
        } else {
            // last trade price
            supplier.getSupplies().add(new Supply(stock, supply.getQuantity(), stock.getPrice(), supplier));
            if (stock.getBestAsk().compareTo(stock.getPrice()) > 0) {
                stock.setBestAsk(stock.getPrice());
            }
        }
    }

    private BigDecimal matchSupply(List<Demand> demands, Order supply, User supplier, Stock stock) {
        BigDecimal lastDemandPrice = null;
        for (Demand demand : demands) {
            if (demand.getQuantity().compareTo(supply.getQuantity()) >= 0) {
                Order match = new Order(supply.getStock(), supply.getQuantity(), demand.getPrice());
                if (!supplierMatchProcessed(supplier, demand, match, stock)) {
                    continue;
                }

                if (demand.getQuantity().compareTo(supply.getQuantity()) == 0) {
                    demandRepo.delete(demand);
                }
                trade(stock, match, demand.getUser(), supplier);
                return BigDecimal.valueOf(-1);
            } else {
                Order match = new Order(supply.getStock(), demand.getQuantity(), demand.getPrice());
                if (!supplierMatchProcessed(supplier, demand, match, stock)) {
                    continue;
                }

                demandRepo.delete(demand);
                trade(stock, match, demand.getUser(), supplier);
                supply.setQuantity(supply.getQuantity() - demand.getQuantity());
                lastDemandPrice = demand.getPrice();
            }
        }

        return lastDemandPrice;
    }

    private boolean supplierMatchProcessed(User seller, Demand demand, Order match, Stock stock) {
        try {
            demand.getUser().buyStock(match, stock);
        } catch (MarketCheckedException e) {
            logger.debug(Constants.NOT_ENOUGH_BALANCE_LOG);
            demandRepo.delete(demand);
            return false;
        }

        try {
            seller.sellStock(match);
        } catch (MarketCheckedException e) {
            logger.debug(e.getMessage());
            throw new MarketException(Constants.NOT_ENOUGH_SUPPLY);
        }

        return true;
    }

    private void trade(Stock stock, Order match, User consumer, User supplier) {
        tradeRepo.save(new Trade(stock, match.getQuantity(), match.getPrice(), consumer, supplier));
        stock.setPrice(match.getPrice());
    }

    @Transactional
    public void consume(Order demand, String username) {
        Optional<User> consumerOpt = userRepo.findByUsername(username);

        if (consumerOpt.isPresent()) {
            Optional<Stock> stockOpt = stockRepo.findByName(demand.getStock());

            if (stockOpt.isPresent()) {
                tradeDemand(stockOpt.get(), demand, consumerOpt.get());
            } else {
                throw new MarketException(Constants.NO_STOCK_MSG);
            }

        } else {
            throw new MarketException(Constants.NO_USR_MSG);
        }
    }

    private void tradeDemand(Stock stock, Order demand, User consumer) {
        List<Supply> supplies = supplyRepo.findAllByStockOrderByPriceAsc(demand.getStock());

        if (!supplies.isEmpty()) {
            BigDecimal lastSupplyPrice = matchDemand(supplies, demand, consumer, stock);

            if (lastSupplyPrice != null && BigDecimal.valueOf(-1).compareTo(lastSupplyPrice) != 0) {
                consumer.getDemands().add(new Demand(stock, demand.getQuantity(), lastSupplyPrice, consumer));
                if (stock.getBestBid().compareTo(lastSupplyPrice) < 0) {
                    stock.setBestAsk(lastSupplyPrice);
                }
            } else if (lastSupplyPrice == null) {
                // last trade price
                consumer.getDemands().add(new Demand(stock, demand.getQuantity(), stock.getPrice(), consumer));
                if (stock.getBestBid().compareTo(stock.getPrice()) < 0) {
                    stock.setBestAsk(stock.getPrice());
                }
            }
        } else {
            // last trade price
            consumer.getDemands().add(new Demand(stock, demand.getQuantity(), stock.getPrice(), consumer));
            if (stock.getBestBid().compareTo(stock.getPrice()) < 0) {
                stock.setBestAsk(stock.getPrice());
            }
        }
    }

    private BigDecimal matchDemand(List<Supply> supplies, Order demand, User consumer, Stock stock) {
        BigDecimal lastSupplyPrice = null;
        for (Supply supply : supplies) {
            if (supply.getQuantity().compareTo(demand.getQuantity()) >= 0) {
                Order match = new Order(demand.getStock(), demand.getQuantity(), supply.getPrice());

                if (!consumerMatchProcessed(consumer, supply, match, stock)) {
                    continue;
                }

                if (supply.getQuantity().compareTo(demand.getQuantity()) == 0) {
                    supplyRepo.delete(supply);
                }
                trade(stock, match, consumer, supply.getUser());
                return BigDecimal.valueOf(-1);
            } else {
                Order match = new Order(demand.getStock(), supply.getQuantity(), supply.getPrice());

                if (!consumerMatchProcessed(consumer, supply, match, stock)) {
                    continue;
                }

                supplyRepo.delete(supply);
                trade(stock, match, consumer, supply.getUser());
                demand.setQuantity(demand.getQuantity() - supply.getQuantity());
                lastSupplyPrice = supply.getPrice();
            }
        }

        return lastSupplyPrice;
    }

    private boolean consumerMatchProcessed(User consumer, Supply supply, Order match, Stock stock) {
        try {
            supply.getUser().sellStock(match);
        } catch (MarketCheckedException e) {
            logger.debug(Constants.NOT_ENOUGH_STOCK_LOG);
            supplyRepo.delete(supply);
            return false;
        }

        try {
            consumer.buyStock(match, stock);
        } catch (MarketCheckedException e) {
            logger.debug(e.getMessage());
            throw new MarketException(Constants.NOT_ENOUGH_BALANCE);
        }

        return true;
    }

    private Stock checkStock(Order supply, User user) {
        List<UserStock> userStocks = user.getUserStocks();

        if (userStocks != null && !userStocks.isEmpty()) {
            Optional<UserStock> userStock = userStocks.stream()
                    .filter(st -> st.getStock().getName().equals(supply.getStock()))
                    .findFirst();
            if (userStock.isPresent() && userStock.get().getQuantity().compareTo(supply.getQuantity()) >= 0) {
                return userStock.get().getStock();
            }
        }
        throw new MarketException("Insufficient stock!");
    }

    public List<Supply> getSupplies(String username) {
        Optional<User> user = userRepo.findByUsername(username);

        if (user.isPresent()) {
            return user.get().getSupplies();
        } else {
            throw new MarketException(Constants.NO_USR_MSG);
        }
    }

    public List<Demand> getDemands(String username) {
        Optional<User> user = userRepo.findByUsername(username);

        if (user.isPresent()) {
            return user.get().getDemands();
        } else {
            throw new MarketException(Constants.NO_USR_MSG);
        }
    }
}
