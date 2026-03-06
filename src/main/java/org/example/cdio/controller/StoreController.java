package org.example.cdio.controller;

import org.example.cdio.dto.CartItem;
import org.example.cdio.entity.*;
import org.example.cdio.repository.ProductRepository;
import org.example.cdio.repository.StoreRepository;
import org.example.cdio.repository.UserRepository;
import org.example.cdio.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CartService cartService;

    // dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "8") int size) {

        Page<Product> productPage = productRepository.findByIsActiveTrue(PageRequest.of(page, size));

        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("cartCount", cartService.getTotalCount());

        return "store/store-dashboard";
    }

    // profile
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            model.addAttribute("user", user);
            model.addAttribute("store", user.getStore());
        }

        return "store/store-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            Principal principal,
            @RequestParam("fullName") String fullName,
            @RequestParam("representativeName") String representativeName,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address
    ) {

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {

            User user = userOpt.get();
            user.setFullName(fullName);

            userRepository.save(user);

            Store store = user.getStore();

            if (store != null) {

                store.setRepresentativeName(representativeName);
                store.setPhone(phone);
                store.setAddress(address);

                storeRepository.save(store);
            }
        }

        return "redirect:/store/profile?success";
    }

    // thêm vào giỏ
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestParam Long id,
                                       @RequestParam String name,
                                       @RequestParam double price) {

        cartService.addProduct(id, name, price);

        return ResponseEntity.ok(Map.of(
                "totalCount", cartService.getTotalCount()
        ));
    }

    // trang giỏ hàng
    @GetMapping("/cart")
    public String showCartPage(Model model) {

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("totalCount", cartService.getTotalCount());

        return "store/store-cart";
    }

    // cập nhật số lượng
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCart(@RequestParam Long id,
                                        @RequestParam int quantity) {

        cartService.updateQuantity(id, quantity);

        double itemTotal = 0;

        Optional<CartItem> itemOpt =
                cartService.getCartItems().stream()
                        .filter(i -> i.getProductId().equals(id))
                        .findFirst();

        if (itemOpt.isPresent()) {
            itemTotal = itemOpt.get().getPrice() * itemOpt.get().getQuantity();
        }

        return ResponseEntity.ok(Map.of(
                "itemTotal", itemTotal,
                "totalCount", cartService.getTotalCount(),
                "totalPrice", cartService.getTotalPrice()
        ));
    }

    // xóa khỏi giỏ
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long id) {

        cartService.removeProduct(id);

        return "redirect:/store/cart";
    }

    // Chuc nang don hang da duoc tat.
    @PostMapping("/order/create")
    public String createOrder(RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", "Chuc nang don hang tam thoi da tat.");
        return "redirect:/store/cart";
    }
}