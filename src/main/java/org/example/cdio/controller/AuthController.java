package org.example.cdio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderStatus;
import org.example.cdio.dto.StoreRegisterRequest;
import org.example.cdio.entity.OrderItem;
import org.example.cdio.entity.Store;
import org.example.cdio.repository.OrderItemRepository;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.repository.UserRepository;
import org.example.cdio.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

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
        return "redirect:/staff/dashboard";
    }

     
    
    @GetMapping("/admin/dashboard")
    public String admin() {
        return "admin/dashboard";
    }
//    @GetMapping("/store/dashboard")
//    public String store() {
//        return "store/dashboard";
//    }

    @GetMapping("/staff/dashboard")
    public String staff(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values()));

        List<OrderItem> allItems = orderItemRepository.findAllWithDetails();

        if (allItems.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            return "staff/dashboard";
        }

        Long resolvedOrderId = orderId != null ? orderId : allItems.get(0).getOrder().getId();
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithDetails(resolvedOrderId);

        if (orderItems.isEmpty()) {
            model.addAttribute("orderItems", Collections.emptyList());
            return "staff/dashboard";
        }

        OrderItem firstItem = orderItems.get(0);
        Store store = firstItem.getOrder().getStore();

        model.addAttribute("orderItems", orderItems);
        model.addAttribute("orderId", firstItem.getOrder().getId());

        model.addAttribute("deliverySlipCode", firstItem.getOrder().getId());
        model.addAttribute("orderCode", firstItem.getProduct().getId());

        model.addAttribute("createdDate", firstItem.getOrder().getCreatedAt());
        model.addAttribute("deliveryStatus", firstItem.getOrder().getStatus());
        model.addAttribute("orderTotal", firstItem.getOrder().getTotalAmount());
        model.addAttribute("shipperId", firstItem.getOrder().getCreatedBy());
        model.addAttribute("incident", firstItem.getOrder().getRejectedReason());

        if (store != null) {
            model.addAttribute("customerName", store.getRepresentativeName() != null ? store.getRepresentativeName() : store.getName());
            model.addAttribute("customerPhone", store.getPhone());
            model.addAttribute("deliveryAddress", store.getAddress());
        }

        if (firstItem.getOrder().getCreatedBy() != null) {
            userRepository.findById(firstItem.getOrder().getCreatedBy())
                    .ifPresent(user -> model.addAttribute("shipperName", user.getFullName()));
        }

        return "staff/dashboard";
    }

    @PostMapping("/staff/delivery/update")
    public String updateDelivery(
            @RequestParam("orderId") Long orderId,
            @RequestParam("deliveryDate") LocalDate deliveryDate,
            @RequestParam("deliveredBy") Long deliveredBy,
            @RequestParam("deliveryStatus") OrderStatus deliveryStatus,
            @RequestParam(value = "incident", required = false) String incident
            , RedirectAttributes redirectAttributes
    ) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để cập nhật.");
            return "redirect:/staff/dashboard";
        }

        if (!userRepository.existsById(deliveredBy)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người giao không tồn tại trong bảng users.");
            return "redirect:/staff/dashboard?orderId=" + orderId;
        }

        order.setCreatedAt(LocalDateTime.of(deliveryDate, order.getCreatedAt().toLocalTime()));
        order.setCreatedBy(deliveredBy);
        order.setStatus(deliveryStatus);
        order.setRejectedReason(incident);

        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giao hàng vào database.");

        return "redirect:/staff/dashboard?orderId=" + orderId;
    }
}
