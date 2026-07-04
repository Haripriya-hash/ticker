package com.rts.ticker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the response from Finnhub's /quote endpoint.
 * See https://finnhub.io/docs/api/quote
 *
 * c = current price, h = high, l = low, o = open, pc = previous close, t = timestamp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubQuote(
        @JsonProperty("c") Double currentPrice,
        @JsonProperty("h") Double high,
        @JsonProperty("l") Double low,
        @JsonProperty("o") Double open,
        @JsonProperty("pc") Double previousClose,
        @JsonProperty("t") Long timestamp
) {
    /** Finnhub returns all zeros for an unrecognized symbol. */
    public boolean isEmpty() {
        return (currentPrice == null || currentPrice == 0)
                && (open == null || open == 0)
                && (previousClose == null || previousClose == 0);
    }
}
