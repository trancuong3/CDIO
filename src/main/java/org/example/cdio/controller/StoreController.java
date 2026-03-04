package org.example.cdio.controller;

import org.example.cdio.entity.Product;
import org.example.cdio.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "8") int size) {

        Page<Product> productPage =
                productRepository.findByIsActiveTrue(PageRequest.of(page, size));

        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);

        return "store/store-dashboard";
    }
}