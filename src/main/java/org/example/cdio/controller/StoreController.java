package org.example.cdio.controller;

import org.example.cdio.entity.Product;
import org.example.cdio.entity.Store;
import org.example.cdio.entity.User;
import org.example.cdio.repository.ProductRepository;
import org.example.cdio.repository.StoreRepository;
import org.example.cdio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
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

    // --- CÁC HÀM XỬ LÝ PROFILE ---

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
}