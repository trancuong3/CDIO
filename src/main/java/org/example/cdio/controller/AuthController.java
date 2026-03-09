package org.example.cdio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cdio.dto.StoreRegisterRequest;
import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderItem;
import org.example.cdio.entity.OrderStatus;
import org.example.cdio.entity.Store;
import org.example.cdio.repository.OrderItemRepository;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.repository.UserRepository;
import org.example.cdio.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

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
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/store-register";
        }
    }

    @GetMapping("/admin/dashboard")
    public String admin() {
        return "admin/dashboard";
    }

    @GetMapping("/staff/dashboard")
    public String staffDashboardRedirect() {
        return "redirect:/shipper/dashboard";
    }
}
