package com.rts.ticker.controller;

import com.rts.ticker.dto.ChangePasswordForm;
import com.rts.ticker.service.SignupService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChangePasswordController {

    private final SignupService signupService;

    public ChangePasswordController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "change-password";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "New password and confirmation do not match.");
            return "change-password";
        }

        try {
            signupService.changePassword(authentication.getName(), form.getCurrentPassword(), form.getNewPassword());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "change-password";
        }

        model.addAttribute("successMessage", "Your password has been updated.");
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "change-password";
    }
}
