package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Delivery;
import org.example.cdio.entity.DeliveryStatus;
import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderItem;
import org.example.cdio.entity.OrderStatus;
import org.example.cdio.entity.User;
import org.example.cdio.repository.DeliveryRepository;
import org.example.cdio.repository.OrderItemRepository;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shipper")
public class ShipperController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "deliveryId", required = false) Long deliveryId,
                            Principal principal,
                            Model model) {
        User currentUser = currentUser(principal);
        String shipperName = displayName(currentUser);

        List<Order> availableOrders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.APPROVED)
                .stream()
                .filter(order -> !deliveryRepository.existsByOrderId(order.getId()))
                .toList();

        List<Delivery> myDeliveries = deliveryRepository.findByDelivererNameOrderByShippedAtDesc(shipperName);
        Delivery selectedDelivery = resolveSelectedDelivery(myDeliveries, deliveryId);

        List<OrderItem> orderItems = Collections.emptyList();
        Order selectedOrder = null;
        if (selectedDelivery != null) {
            selectedOrder = orderRepository.findById(selectedDelivery.getOrderId()).orElse(null);
            if (selectedOrder != null) {
                orderItems = orderItemRepository.findByOrderIdWithDetails(selectedOrder.getId());
            }
        }

        model.addAttribute("shipperName", shipperName);
        model.addAttribute("availableOrders", availableOrders);
        model.addAttribute("myDeliveries", myDeliveries);
        model.addAttribute("selectedDelivery", selectedDelivery);
        model.addAttribute("selectedOrder", selectedOrder);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("deliveryStatusOptions", List.of(DeliveryStatus.SHIPPED, DeliveryStatus.DELIVERED, DeliveryStatus.FAILED));
        return "shipper/dashboard";
    }

    @PostMapping("/orders/{orderId}/accept")
    public String acceptOrder(@PathVariable Long orderId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        User currentUser = currentUser(principal);
        String shipperName = displayName(currentUser);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang"));

        if (order.getStatus() != OrderStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Don hang khong o trang thai san sang giao.");
            return "redirect:/shipper/dashboard";
        }

        if (deliveryRepository.existsByOrderId(orderId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Don hang da duoc nhan giao.");
            return "redirect:/shipper/dashboard";
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setDelivererName(shipperName);
        delivery.setShippedAt(LocalDateTime.now());
        delivery.setStatus(DeliveryStatus.SHIPPED);
        deliveryRepository.save(delivery);

        order.setStatus(OrderStatus.DELIVERING);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("successMessage", "Nhan don thanh cong.");
        return "redirect:/shipper/dashboard";
    }

    @PostMapping("/deliveries/{deliveryId}/update")
    public String updateDelivery(@PathVariable Long deliveryId,
                                 @RequestParam("status") String status,
                                 @RequestParam(value = "issueNote", required = false) String issueNote,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        String shipperName = displayName(currentUser(principal));

        Delivery delivery = deliveryRepository.findByIdAndDelivererName(deliveryId, shipperName)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phieu giao."));

        DeliveryStatus dbStatus = normalizeDeliveryStatus(status);
        delivery.setStatus(dbStatus);
        delivery.setIssueNote(issueNote);
        if (dbStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
        }
        deliveryRepository.save(delivery);

        Order order = orderRepository.findById(delivery.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang."));
        order.setStatus(toOrderStatus(dbStatus));
        if (dbStatus == DeliveryStatus.FAILED) {
            order.setRejectedReason(issueNote);
        }
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat trang thai giao hang.");
        return "redirect:/shipper/dashboard?deliveryId=" + deliveryId;
    }

    private Delivery resolveSelectedDelivery(List<Delivery> deliveries, Long deliveryId) {
        if (deliveries == null || deliveries.isEmpty()) {
            return null;
        }
        if (deliveryId == null) {
            return deliveries.get(0);
        }
        return deliveries.stream()
                .filter(d -> d.getId().equals(deliveryId))
                .findFirst()
                .orElse(deliveries.get(0));
    }

    private User currentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user dang nhap"));
    }

    private String displayName(User user) {
        return (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName()
                : user.getUsername();
    }

    private DeliveryStatus normalizeDeliveryStatus(String status) {
        if (status == null || status.isBlank()) {
            return DeliveryStatus.NOT_SHIPPED;
        }

        return switch (status.trim().toUpperCase()) {
            case "NOT_SHIPPED" -> DeliveryStatus.NOT_SHIPPED;
            case "SHIPPED", "DELIVERING" -> DeliveryStatus.SHIPPED;
            case "DELIVERED" -> DeliveryStatus.DELIVERED;
            case "FAILED", "CANCELLED", "CANCELED", "REJECTED" -> DeliveryStatus.FAILED;
            default -> DeliveryStatus.NOT_SHIPPED;
        };
    }

    private OrderStatus toOrderStatus(DeliveryStatus deliveryStatus) {
        return switch (deliveryStatus) {
            case DELIVERED -> OrderStatus.DELIVERED;
            case FAILED -> OrderStatus.CANCELLED;
            case SHIPPED, NOT_SHIPPED -> OrderStatus.DELIVERING;
            default -> OrderStatus.DELIVERING;
        };
    }
}