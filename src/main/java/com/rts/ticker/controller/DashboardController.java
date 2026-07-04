package com.rts.ticker.controller;

import com.rts.ticker.dto.FinnhubQuote;
import com.rts.ticker.dto.StockLookupForm;
import com.rts.ticker.service.FinnhubService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class DashboardController {

    private final FinnhubService finnhubService;

    public DashboardController(FinnhubService finnhubService) {
        this.finnhubService = finnhubService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        if (!model.containsAttribute("stockLookupForm")) {
            model.addAttribute("stockLookupForm", new StockLookupForm());
        }
        return "dashboard";
    }

    @PostMapping("/stock-lookup")
    public String lookupStock(@Valid @ModelAttribute("stockLookupForm") StockLookupForm form,
                               BindingResult bindingResult,
                               Model model,
                               Authentication authentication) {
        model.addAttribute("username", authentication.getName());

        if (bindingResult.hasErrors()) {
            return "dashboard";
        }

        try {
            Optional<FinnhubQuote> quote = finnhubService.getQuote(form.getSymbol());
            if (quote.isEmpty()) {
                model.addAttribute("errorMessage", "No data found for symbol \"" + form.getSymbol().toUpperCase() + "\".");
            } else {
                model.addAttribute("symbol", form.getSymbol().toUpperCase());
                model.addAttribute("openPrice", quote.get().open());
            }
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "dashboard";
    }
}
