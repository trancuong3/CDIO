package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.*;
import org.example.cdio.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

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

        Order o = orderRepo.findById(id).orElseThrow();

        for (OrderItem i : o.getItems()) {
            inventoryService.stockOut(i.getProduct().getId(), i.getQuantity());
        }

        o.setStatus(OrderStatus.APPROVED);
    }
}
