package com.market.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class QuotePriceHistory {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
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
