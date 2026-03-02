package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {
    private final OrderRepository repo;
    private final OrderService service;
    @GetMapping
    public String list(Model m){
        m.addAttribute("orders",repo.findAll());
        return "admin/order-list";
    }
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id){

        service.approve(id);
        return "redirect:/admin/orders";
    }
}
