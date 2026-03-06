package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/store/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public String createOrder() {

        orderService.createOrder();   // tạo đơn + gửi email

        return "redirect:/store/cart";
    }
}