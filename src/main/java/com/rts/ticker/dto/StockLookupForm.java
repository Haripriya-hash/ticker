package com.rts.ticker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StockLookupForm {

    @NotBlank(message = "Please enter a stock symbol")
    @Pattern(regexp = "^[A-Za-z.]{1,10}$", message = "Enter a valid stock symbol, e.g. AAPL")
    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
