package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderStatus;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository repo;
    private final OrderService service;

    @GetMapping
    public String list(Model model){
        model.addAttribute("orders", repo.findAll());
        return "admin/order-list";
    }

    @PostMapping("/confirm-payment/{orderId}")
    public String confirmPayment(@PathVariable Long orderId){

        Order order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn"));

        order.setStatus(OrderStatus.PAID);

        repo.save(order);

        return "redirect:/admin/orders";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id){

        service.approve(id);

        return "redirect:/admin/orders";
    }

}