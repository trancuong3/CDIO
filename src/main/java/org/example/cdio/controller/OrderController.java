package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/store/order")
public class OrderController {

    private final OrderRepository orderRepository;

    // trang thông báo đơn đã được duyệt
    @GetMapping("/order-approved")
    public String orderApproved(){
        return "order-approved";
    }

    // API kiểm tra trạng thái đơn hàng
    @GetMapping("/api/order-status/{id}")
    @ResponseBody
    public String getStatus(@PathVariable Long id){

        return orderRepository.findById(id)
                .map(o -> o.getStatus().name())
                .orElse("NOT_FOUND");
    }
}