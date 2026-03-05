package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/order-approved")
    public String orderApproved(){
        return "order-approved";
    }

    @GetMapping("/api/order-status/{id}")
    @ResponseBody
    public String getStatus(@PathVariable Long id){
        return orderRepository.findById(id)
                .map(o -> o.getStatus().name())
                .orElse("NOT_FOUND");
    }
}