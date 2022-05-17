package com.market.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuotePriceHistory {
    private LocalDateTime date;
    private BigDecimal value;

    public QuotePriceHistory(LocalDateTime date, BigDecimal value) {
        this.date = date;
        this.value = value;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }
}
