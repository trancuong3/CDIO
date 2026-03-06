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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
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
    public String staffDashboard(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        return loadDashboard(orderId, model, "staff/dashboard");
    }

    @GetMapping({"/shipper-dashboard", "/shipper/dashboard"})
    public String shipperDashboard(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        return loadDashboard(orderId, model, "shipper/shipper-dashboard");
    }

    @PostMapping("/staff/delivery/update")
    public String updateDelivery(
            @RequestParam("orderId") Long orderId,
            @RequestParam("deliveryDate") LocalDate deliveryDate,
            @RequestParam("deliveredBy") Long deliveredBy,
            @RequestParam("deliveryStatus") OrderStatus deliveryStatus,
            @RequestParam(value = "incident", required = false) String incident,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes
    ) {
        String redirectBase = (referer != null && referer.contains("/shipper"))
                ? "/shipper-dashboard"
                : "/staff/dashboard";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay don hang de cap nhat.");
            return "redirect:" + redirectBase;
        }

        if (!userRepository.existsById(deliveredBy)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nguoi giao khong ton tai trong bang users.");
            return "redirect:" + redirectBase + "?orderId=" + orderId;
        }

        LocalDateTime currentCreatedAt = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
        order.setCreatedAt(LocalDateTime.of(deliveryDate, currentCreatedAt.toLocalTime()));
        order.setCreatedBy(deliveredBy);
        order.setStatus(deliveryStatus);
        order.setRejectedReason(incident);

        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat giao hang vao database.");

        return "redirect:" + redirectBase + "?orderId=" + orderId;
    }

    private String loadDashboard(Long orderId, Model model, String viewName) {
        model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values()));

        List<OrderItem> allItems = orderItemRepository.findAllWithDetails();
        if (allItems.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            return viewName;
        }

        Long resolvedOrderId = orderId != null ? orderId : allItems.get(0).getOrder().getId();
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithDetails(resolvedOrderId);
        if (orderItems.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            return viewName;
        }

        OrderItem firstItem = orderItems.get(0);
        Order order = firstItem.getOrder();
        Store store = order.getStore();

        model.addAttribute("orderItems", orderItems);
        model.addAttribute("orderId", order.getId());
        model.addAttribute("deliverySlipCode", order.getId());
        model.addAttribute("orderCode", firstItem.getProduct() != null ? firstItem.getProduct().getId() : null);
        model.addAttribute("createdDate", order.getCreatedAt());
        model.addAttribute("deliveryStatus", order.getStatus());
        model.addAttribute("shipperId", order.getCreatedBy());
        model.addAttribute("incident", order.getRejectedReason());
        model.addAttribute("orderTotal", order.getTotalAmount());

        if (store != null) {
            model.addAttribute("customerName", store.getRepresentativeName() != null ? store.getRepresentativeName() : store.getName());
            model.addAttribute("customerPhone", store.getPhone());
            model.addAttribute("deliveryAddress", store.getAddress());
        }

        if (order.getCreatedBy() != null) {
            userRepository.findById(order.getCreatedBy())
                    .ifPresent(user -> model.addAttribute("shipperName", user.getFullName()));
        }

        return viewName;
    }
}
