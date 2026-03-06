package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.dto.CartItem;
import org.example.cdio.entity.*;
import org.example.cdio.repository.InventoryRepository;
import org.example.cdio.repository.OrderRepository;
import org.example.cdio.repository.ProductRepository;
import org.example.cdio.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final OrderRepository orderRepo;
    private final InventoryService inventoryService;
    private final EmailService emailService;

    public Order create(Store store, Map<Product, Integer> items) {

        Order order = new Order();
        order.setStore(store);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (var e : items.entrySet()) {

            Product p = e.getKey();
            Integer qty = e.getValue();

            BigDecimal price = p.getWholesalePrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(qty);
            oi.setUnitPrice(price);
            oi.setLineTotal(lineTotal);

            order.getItems().add(oi);

            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepo.save(order);

        String subject = "Đơn hàng mới từ hệ thống Khô Gà";

        String content = "Xin chào,\n\n"
                + "Một đơn hàng mới vừa được tạo.\n"
                + "Mã đơn hàng: #" + savedOrder.getId() + "\n"
                + "Tổng tiền: " + savedOrder.getTotalAmount() + " VNĐ\n"
                + "Trạng thái: " + savedOrder.getStatus() + "\n\n"
                + "Vui lòng đăng nhập hệ thống để kiểm tra chi tiết.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống quản lý Khô Gà Độ Chó.";

        emailService.sendEmail(
                "testgpttrial22@gmail.com",
                subject,
                content
        );

        return savedOrder;
    }

    // ADMIN DUYỆT
    public void approve(Long id) {

        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn"));

        if (o.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Đơn chưa thanh toán");
        }

        for (OrderItem i : o.getItems()) {

            Inventory inv = inventoryRepository
                    .findByProductId(i.getProduct().getId())
                    .orElseGet(() -> {

                        Inventory newInv = Inventory.builder()
                                .product(i.getProduct())
                                .quantity(0)
                                .minLevel(5)
                                .build();

                        return inventoryRepository.save(newInv);
                    });

            int thiếu = i.getQuantity() - inv.getQuantity();

            if (thiếu > 0) {
                inventoryService.stockIn(i.getProduct().getId(), thiếu);
            }

            inventoryService.stockOut(i.getProduct().getId(), i.getQuantity());
        }

        o.setStatus(OrderStatus.APPROVED);
        orderRepo.save(o);

        String subject = "Đơn hàng đã được duyệt";

        String content = "Xin chào,\n\n"
                + "Đơn hàng #" + o.getId() + " của bạn đã được duyệt thành công.\n"
                + "Tổng tiền: " + o.getTotalAmount() + " VNĐ\n"
                + "Trạng thái: " + o.getStatus() + "\n\n"
                + "Vui lòng chuẩn bị nhận hàng.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống Khô Gà.";

        emailService.sendEmail(
                "testgpttrial22@gmail.com",
                subject,
                content
        );
    }

    public boolean checkInventory(Order order){

        for(OrderItem item : order.getItems()){

            Inventory inv = inventoryRepository
                    .findByProductId(item.getProduct().getId())
                    .orElseThrow();

            if(inv.getQuantity() < item.getQuantity()){
                return false;
            }
        }

        return true;
    }

    public Order createOrderFromCart(Principal principal) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username).orElseThrow();

        Store store = user.getStore();

        Order order = new Order();
        order.setStore(store);
        order.setCreatedBy(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartService.getCartItems()) {

            Product product = productRepository
                    .findById(cartItem.getProductId())
                    .orElseThrow();

            int qty = cartItem.getQuantity();

            BigDecimal price = product.getWholesalePrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(qty);
            oi.setUnitPrice(price);
            oi.setLineTotal(lineTotal);

            order.getItems().add(oi);

            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        return orderRepo.save(order);
    }

    public void createOrder() {

        System.out.println("Order created!");

        String subject = "Đơn hàng mới từ hệ thống Khô Gà";

        String content = "Xin chào,\n\n"
                + "Một đơn hàng mới vừa được tạo trong hệ thống.\n"
                + "Vui lòng đăng nhập vào hệ thống để kiểm tra chi tiết.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống quản lý Khô Gà.";

        emailService.sendEmail(
                "testgpttrial22@gmail.com",
                subject,
                content
        );
    }
}