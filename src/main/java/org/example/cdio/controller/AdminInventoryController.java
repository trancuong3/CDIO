    package org.example.cdio.controller;

    import lombok.RequiredArgsConstructor;
    import org.example.cdio.repository.ProductRepository;
    import org.example.cdio.service.InventoryService;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;

    @Controller
    @RequiredArgsConstructor
    @RequestMapping("/admin/inventory")
    public class AdminInventoryController {
        private final InventoryService inventoryService;
        private final ProductRepository productRepo;
        @GetMapping
        public String list(Model model) {

            model.addAttribute("inventories",
                    inventoryService.inventoryView());

            model.addAttribute("products",
                    productRepo.findAll());

            return "admin/inventory";
        }
        @PostMapping("/in")
        public String stockIn(
                @RequestParam Long productId,
                @RequestParam Integer qty) {

            if (qty <= 0) return "redirect:/admin/inventory";

            inventoryService.stockIn(productId, qty);

            return "redirect:/admin/inventory";
        }
        @PostMapping("/out")
        public String stockOut(
                @RequestParam Long productId,
                @RequestParam Integer qty) {

            if (qty <= 0) return "redirect:/admin/inventory";

            inventoryService.stockOut(productId, qty);

            return "redirect:/admin/inventory";
        }
    }
