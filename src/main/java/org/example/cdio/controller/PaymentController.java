package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Order;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.service.CartService;
import org.example.cdio.service.VietQRService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    private final CartService cartService;

    private final OrderRepository orderRepository;
    private final VietQRService vietQRService;

    @GetMapping("/vietqr/{orderId}")
    public String qr(@PathVariable Long orderId, Model model){

        Order order = orderRepository.findById(orderId).orElseThrow();

        String qrUrl = vietQRService.generateQR(
                orderId,
                order.getTotalAmount().doubleValue()
        );

        model.addAttribute("order",order);
        model.addAttribute("qrUrl",qrUrl);

        return "payment/qr";
    }

}