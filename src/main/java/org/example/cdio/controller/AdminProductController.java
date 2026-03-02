package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Product;
import org.example.cdio.service.InventoryService;
import org.example.cdio.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final InventoryService inventoryService;
    @GetMapping
    public String list(Model model){

        model.addAttribute("inventories",
                inventoryService.inventoryView());

        return "admin/product-list";
    }@GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("product", new Product());

        return "admin/product-form";
    }@PostMapping("/save")
    public String save(@ModelAttribute Product product) {

        productService.save(product);

        return "redirect:/admin/products";
    }

    // ================= EDIT =================
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        model.addAttribute("product",
                productService.findById(id));

        return "admin/product-form";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        productService.delete(id);

        return "redirect:/admin/products";
    }
}
