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

        return orderRepo.save(order);
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

                        // tạo inventory mới nếu chưa có
                        Inventory newInv = Inventory.builder()
                                .product(i.getProduct())
                                .quantity(0)
                                .minLevel(5)
                                .build();

                        return inventoryRepository.save(newInv);
                    });

            int thiếu = i.getQuantity() - inv.getQuantity();

            // nếu thiếu kho -> nhập thêm
            if (thiếu > 0) {
                inventoryService.stockIn(i.getProduct().getId(), thiếu);
            }

            // xuất kho
            inventoryService.stockOut(i.getProduct().getId(), i.getQuantity());
        }

        o.setStatus(OrderStatus.APPROVED);

        orderRepo.save(o);
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
        order.setCreatedBy(user);   // ⭐ thêm dòng này
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
}
