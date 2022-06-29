package com.market.repo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.market.entity.PriceHistory;
import com.market.entity.Stock;

@Component
public class RandomDataLoader implements ApplicationRunner {

    private static final int STOCK_COUNT = 100;
    private static final int DECIMAL_PRECISION_LIMIT = 100;
    private static final int DECIMAL_PRECISION = 2;
    private static final int DECIMAL_LIMIT = 1000;
    private static final RoundingMode DECIMAL_ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final int RATE_LIMIT = 10;

    private static int MIN_CHAR = (char) 'a';
    private static int MAX_CHAR = (char) 'z';
    private static int WORD_COUNT_LIMIT = 3;
    private static int MAX_WORD_LENGTH = 7;
    private static int MIN_WORD_LENGTH = 2;

    private static int MAX_PRICE_CHANGE = 10;
    private static int PRICE_HISTORY_FREQUENCY = 10; // minutes
    private static int PRICE_HISTORY_LENGTH_LIMIT = 50;

    private final StockRepo stockRepo;
    private final TradeRepo tradeRepo;
    private final UserRepo userRepo;

    private Random rand = new Random();

    public RandomDataLoader(StockRepo stockRepo, UserRepo userRepo, TradeRepo tradeRepo) {
        this.stockRepo = stockRepo;
        this.userRepo = userRepo;
        this.tradeRepo = tradeRepo;

    }

    public void run(ApplicationArguments args) {
        tradeRepo.deleteAll();
        userRepo.deleteAll();
        stockRepo.deleteAll();

        for (int i = 0; i < STOCK_COUNT; i++) {
            stockRepo.save(createRandomStock());
        }
    }

    private Stock createRandomStock() {
        BigDecimal bestAsk = createRandomDecimalNumber(null, false);
        BigDecimal price = createRandomDecimalNumber(bestAsk, false);
        BigDecimal bestBid = createRandomDecimalNumber(price, false);

        return new Stock(createRandomStockName(), price, bestBid, bestAsk,
                createRandomDecimalNumber(BigDecimal.valueOf(RATE_LIMIT), true), createRandomPriceHistory(price));
    }

    private BigDecimal createRandomDecimalNumber(BigDecimal upperBound, boolean canBeNegative) {
        BigDecimal randomDecimalPart = new BigDecimal(rand.nextInt(DECIMAL_PRECISION_LIMIT));

        randomDecimalPart = randomDecimalPart.divide(BigDecimal.valueOf(DECIMAL_PRECISION_LIMIT), DECIMAL_PRECISION,
                DECIMAL_ROUNDING_MODE);

        BigDecimal result = null;
        if (upperBound == null || upperBound.intValue() <= 0) {
            result = randomDecimalPart.add(BigDecimal.valueOf(rand.nextInt(DECIMAL_LIMIT)));
        } else {
            result = randomDecimalPart.add(BigDecimal.valueOf(rand.nextInt(upperBound.intValue())));
        }

        if (canBeNegative && rand.nextBoolean()) {
            return result.negate();
        }

        return result;
    }

    private String createRandomStockName() {
        int wordCount = rand.nextInt(WORD_COUNT_LIMIT) + 1;
        StringBuilder result = new StringBuilder();
        int diff = MAX_CHAR - MIN_CHAR + 1;

        for (int i = 0; i < wordCount; i++) {
            int wordLength = rand.nextInt(MAX_WORD_LENGTH) + MIN_WORD_LENGTH;
            int charInt = rand.nextInt(diff) + MIN_CHAR;

            for (int j = 0; j < wordLength; j++) {
                if (j == 0) {
                    result.append(Character.toUpperCase((char) charInt));
                } else {
                    result.append((char) charInt);
                }

                charInt = rand.nextInt(diff) + MIN_CHAR;
            }

            result.append(" ");
        }

        return result.toString().trim();
    }

    private List<PriceHistory> createRandomPriceHistory(BigDecimal lastPrice) {
        List<PriceHistory> prices = new ArrayList<>();

        LocalDateTime currentTime = LocalDateTime.now().minusMinutes(PRICE_HISTORY_FREQUENCY);
        BigDecimal currentPrice = lastPrice;
        int todayOfMonth = currentTime.getDayOfMonth();
        int historyLength = rand.nextInt(PRICE_HISTORY_LENGTH_LIMIT);
        int change = rand.nextInt(MAX_PRICE_CHANGE);
        for (int i = 0; i < historyLength; i++) {
            if (todayOfMonth != currentTime.getDayOfMonth()) {
                break;
            }

            if (rand.nextBoolean()) {
                currentPrice = currentPrice.add(BigDecimal.valueOf(change));
                prices.add(new PriceHistory(currentTime, currentPrice));
            } else {
                currentPrice = currentPrice.subtract(BigDecimal.valueOf(change));
                if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
                    currentPrice = BigDecimal.ZERO;
                }
                prices.add(new PriceHistory(currentTime, currentPrice));
            }

            currentTime = currentTime.minusMinutes(PRICE_HISTORY_FREQUENCY);
        }

        // latest price first
        Collections.reverse(prices);

        return prices;
    }
}
