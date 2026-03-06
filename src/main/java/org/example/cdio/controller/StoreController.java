package org.example.cdio.controller;

import org.example.cdio.dto.CartItem;
import org.example.cdio.entity.*;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.repository.ProductRepository;
import org.example.cdio.repository.StoreRepository;
import org.example.cdio.repository.UserRepository;
import org.example.cdio.service.CartService;
import org.example.cdio.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

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
            Principal principal, //Session user hiện tại
            //Hứng dữ liệu từ các thẻ <input>
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
            userRepository.save(user); //Lưu vào db

            Store store = user.getStore();
            if (store != null) {
                store.setRepresentativeName(representativeName);
                store.setPhone(phone);
                store.setAddress(address);
                storeRepository.save(store); //Lưu vào db
            }
        }
        return "redirect:/store/profile?success"; //Quay lại trang và thông báo thành công
    }

    @PostMapping("/cart/add")
    @ResponseBody // Chú ý: Trả về cục data chứ không load lại trang
    public ResponseEntity<?> addToCart(@RequestParam Long id,
                                       @RequestParam String name,
                                       @RequestParam double price) {
        cartService.addProduct(id, name, price); // Nhét vào giỏ
        // Trả về số lượng mới để Javascript cập nhật UI
        return ResponseEntity.ok(Map.of("totalCount", cartService.getTotalCount()));
    }

    @GetMapping("/cart")
    public String showCartPage(Model model) {
        // Lấy danh sách hàng hóa, tổng tiền, tổng số lượng từ CartService ném ra giao diện
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("totalCount", cartService.getTotalCount());

        return "store/store-cart";
    }

    // --- CẬP NHẬT SỐ LƯỢNG TRONG GIỎ ---
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCart(@RequestParam Long id, @RequestParam int quantity) {
        cartService.updateQuantity(id, quantity);

        // Tính lại thành tiền của riêng món đó
        double itemTotal = 0;
        Optional<CartItem> itemOpt = cartService.getCartItems().stream()
                .filter(i -> i.getProductId().equals(id)).findFirst();
        if (itemOpt.isPresent()) {
            itemTotal = itemOpt.get().getPrice() * itemOpt.get().getQuantity();
        }

        // Trả về 3 con số mới: Tiền món đó, Tổng số lượng giỏ, Tổng tiền giỏ
        return ResponseEntity.ok(Map.of(
                "itemTotal", itemTotal,
                "totalCount", cartService.getTotalCount(),
                "totalPrice", cartService.getTotalPrice()
        ));
    }

    // --- XÓA MÓN HÀNG ---
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long id) {
        cartService.removeProduct(id);
        return "redirect:/store/cart";
    }
    @PostMapping("/checkout")
    public String checkout(Principal principal){

        Order order = orderService.createOrderFromCart(principal);

        boolean enoughStock = orderService.checkInventory(order);

        if(enoughStock){

            // chuyển sang thanh toán momo
            return "redirect:/payment/momo/" + order.getId();

        }else{

            // chuyển sang trạng thái chờ admin
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            return "redirect:/store/orders";
        }
    }
    @PostMapping("/order/create")
    public String createOrder(Principal principal){

        Order order = orderService.createOrderFromCart(principal);

        return "redirect:/payment/vietqr/" + order.getId();
    }
}