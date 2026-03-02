package org.example.cdio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cdio.dto.StoreRegisterRequest;
import org.example.cdio.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    @GetMapping("/store/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new StoreRegisterRequest());
        return "auth/store-register";
    }
    @PostMapping("/store/register")
    public String register(
            @Valid @ModelAttribute("form") StoreRegisterRequest form,
            BindingResult binding,
            Model model
    ) {

        if (binding.hasErrors()) {
            return "auth/store-register";
        }

        try {
            authService.registerStore(form);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/store-register";
        }
    }
    @GetMapping("/admin/dashboard")
    public String admin() {
        return "admin/dashboard";
    }
    @GetMapping("/store/dashboard")
    public String store() {
        return "store/dashboard";
    }
}
