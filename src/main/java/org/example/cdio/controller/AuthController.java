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
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/store/dashboard";
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
        return loadShipperDashboard(orderId, model);
    }

    @GetMapping("/shipper-dashboard")
    public String shipperDashboard(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        return loadShipperDashboard(orderId, model);
    }

    @PostMapping("/staff/delivery/update")
    public String updateDelivery(
            @RequestParam("orderId") Long orderId,
            @RequestParam("deliveryDate") LocalDate deliveryDate,
            @RequestParam("deliveredBy") Long deliveredBy,
            @RequestParam("deliveryStatus") OrderStatus deliveryStatus,
            @RequestParam(value = "incident", required = false) String incident,
            RedirectAttributes redirectAttributes
    ) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để cập nhật.");
            return "redirect:/shipper-dashboard";
        }

        if (!userRepository.existsById(deliveredBy)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người giao không tồn tại trong bảng users.");
            return "redirect:/shipper-dashboard?orderId=" + orderId;
        }

        LocalDateTime currentCreatedAt = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();

        order.setCreatedAt(LocalDateTime.of(deliveryDate, currentCreatedAt.toLocalTime()));
        order.setCreatedBy(deliveredBy);
        order.setStatus(deliveryStatus);
        order.setRejectedReason(incident);

        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giao hàng vào database.");

        return "redirect:/shipper-dashboard?orderId=" + orderId;
    }

    private String loadShipperDashboard(Long orderId, Model model) {
        model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values()));

        List<OrderItem> allItems = orderItemRepository.findAllWithDetails();
        if (allItems.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            return "shipper/shipper-dashboard";
        }

        Long resolvedOrderId = orderId != null ? orderId : allItems.get(0).getOrder().getId();
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithDetails(resolvedOrderId);

        if (orderItems.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            return "shipper/shipper-dashboard";
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

        return "shipper/shipper-dashboard";
    }
//    @GetMapping("/store/dashboard")
//    public String store() {
//        return "store/dashboard";
//    }
}
