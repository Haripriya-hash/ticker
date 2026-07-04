package com.rts.ticker;

import com.rts.ticker.dto.FinnhubQuote;
import com.rts.ticker.service.FinnhubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StockLookupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinnhubService finnhubService;

    @Test
    @WithMockUser(username = "trader1")
    void loggedInUserCanSeeTheDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("username", "trader1"));
    }

    @Test
    @WithMockUser(username = "trader1")
    void loggedInUserCanLookUpAStockAndSeeTheOpeningPrice() throws Exception {
        FinnhubQuote quote = new FinnhubQuote(215.50, 216.80, 213.10, 214.32, 212.90, 1719900000L);
        when(finnhubService.getQuote("AAPL")).thenReturn(Optional.of(quote));

        mockMvc.perform(post("/stock-lookup")
                        .with(csrf())
                        .param("symbol", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("symbol", "AAPL"))
                .andExpect(model().attribute("openPrice", 214.32))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("214.32")));
    }

    @Test
    @WithMockUser(username = "trader1")
    void unknownSymbolShowsAFriendlyErrorInsteadOfCrashing() throws Exception {
        when(finnhubService.getQuote("ZZZZ")).thenReturn(Optional.empty());

        mockMvc.perform(post("/stock-lookup")
                        .with(csrf())
                        .param("symbol", "ZZZZ"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void anonymousUserIsRedirectedAwayFromStockLookup() throws Exception {
        mockMvc.perform(post("/stock-lookup")
                        .with(csrf())
                        .param("symbol", "AAPL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
