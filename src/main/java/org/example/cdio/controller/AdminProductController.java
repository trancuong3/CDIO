package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Product;
import org.example.cdio.service.InventoryService;
import org.example.cdio.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("inventories",
                inventoryService.inventoryView());
        return "admin/product-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(
            @ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile file
    ) throws IOException {

        if (!file.isEmpty()) {
            String base64 = Base64.getEncoder()
                    .encodeToString(file.getBytes());
            product.setImg(base64);
        } else if (product.getId() != null) {

            Product existing = productService.findById(product.getId());
            product.setImg(existing.getImg());
        }

        productService.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("product",
                productService.findById(id));
        return "admin/product-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
}