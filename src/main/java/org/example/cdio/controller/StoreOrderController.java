package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Product;
import org.example.cdio.repository.ProductRepository;
import org.example.cdio.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/store/orders")
public class StoreOrderController {
    private final ProductRepository productRepo;
    private final OrderService orderService;

    @GetMapping("/create")
    public String form(Model m){
        m.addAttribute("products",productRepo.findAll());
        return "store/order-form";
    }
    @PostMapping("/create")
    public String create(
            @RequestParam Long productId,
            @RequestParam Integer qty
    ){

        Product p = productRepo.findById(productId).orElseThrow();

        Map<Product,Integer> map = new HashMap<>();
        map.put(p,qty);

        orderService.create(null,map);

        return "redirect:/store/dashboard";
    }
}
