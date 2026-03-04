package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.dto.ProductInventoryView;
import org.example.cdio.entity.Inventory;
import org.example.cdio.entity.Product;
import org.example.cdio.repository.InventoryRepository;
import org.example.cdio.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final ProductRepository productRepo;

    // ================= NHẬP KHO =================
    public void stockIn(Long productId, Integer qty) {

        if (qty == null || qty <= 0) return;

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Inventory inv = inventoryRepo.findByProductId(productId)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .quantity(0)
                        .minLevel(5)
                        .build());

        inv.setQuantity(inv.getQuantity() + qty);

        inventoryRepo.save(inv);
    }

    // ================= XUẤT KHO =================
    public void stockOut(Long productId, Integer qty) {

        Inventory inv = inventoryRepo.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Không có tồn kho"));

        if (inv.getQuantity() < qty)
            throw new RuntimeException("Không đủ tồn kho");

        inv.setQuantity(inv.getQuantity() - qty);

        inventoryRepo.save(inv);
    }

    public List<ProductInventoryView> inventoryView() {

        return inventoryRepo.findAll()
                .stream()
                // CHỈ LẤY PRODUCT ACTIVE
                .filter(inv ->
                        inv.getProduct() != null &&
                                Boolean.TRUE.equals(inv.getProduct().getIsActive())
                )
                .map(inv -> {

                    Product p = inv.getProduct();

                    LocalDateTime created = p.getCreatedAt();
                    Integer expiryDays = p.getExpiryDays();

                    LocalDateTime expiredAt = null;
                    Long daysLeft = null;

                    if (created != null && expiryDays != null) {

                        expiredAt = created.plusDays(expiryDays);

                        daysLeft = ChronoUnit.DAYS.between(
                                LocalDate.now(),
                                expiredAt.toLocalDate()
                        );
                    }

                    return new ProductInventoryView(
                            p.getId(),
                            p.getName(),
                            p.getWeightGrams(),
                            p.getWholesalePrice(),
                            inv.getQuantity(),
                            created,
                            expiryDays,
                            expiredAt,
                            daysLeft
                    );
                })
                .collect(Collectors.toList());
    }
}