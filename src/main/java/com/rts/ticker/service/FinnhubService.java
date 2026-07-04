package com.rts.ticker.service;

import com.rts.ticker.dto.FinnhubQuote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class FinnhubService {

    private final RestClient restClient;
    private final String apiKey;

    public FinnhubService(RestClient.Builder restClientBuilder,
                           @Value("${finnhub.api-key:}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://finnhub.io/api/v1").build();
        this.apiKey = apiKey;
    }

    /**
     * Looks up the current quote for a stock symbol.
     *
     * @return the quote if the symbol was recognized and the API call succeeded, otherwise empty
     */
    public Optional<FinnhubQuote> getQuote(String symbol) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Finnhub API key is not configured. Set the FINNHUB_API_KEY environment variable.");
        }
        try {
            FinnhubQuote quote = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote")
                            .queryParam("symbol", symbol.toUpperCase())
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(FinnhubQuote.class);

            if (quote == null || quote.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(quote);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}
