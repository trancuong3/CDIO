package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.dto.ProductInventoryView;
import org.example.cdio.entity.*;
import org.example.cdio.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final ProductRepository productRepo;
    private final StockTransactionRepository txRepo;

    public List<Inventory> findAll() {
        return inventoryRepo.findAll();
    }

    // ===== NHẬP KHO =====
    public void stockIn(Long productId, Integer qty) {

        if (qty <= 0) return;

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // lấy tồn kho hiện tại hoặc tạo mới
        Inventory inv = inventoryRepo.findByProductId(productId)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .quantity(0)
                        .minLevel(5)
                        .build());

        // =====================
        // CỘNG TỒN
        // =====================
        inv.setQuantity(inv.getQuantity() + qty);
        inventoryRepo.save(inv);

        // =====================
        // LOG GIAO DỊCH
        // =====================
        StockTransaction tx = StockTransaction.builder()
                .product(product)
                .type(StockType.IN)
                .quantity(qty)
                .transAt(LocalDateTime.now())
                .note("Nhập kho")
                .createdBy(1L)   // tạm hardcode admin = 1
                .build();

        txRepo.save(tx);
    }




    // ===== XUẤT =====
    public void stockOut(Long productId, Integer qty) {

        Inventory inv = inventoryRepo.findByProductId(productId)
                .orElseThrow();

        if (inv.getQuantity() < qty)
            throw new RuntimeException("Không đủ tồn kho");

        inv.setQuantity(inv.getQuantity() - qty);

        inventoryRepo.save(inv);

        txRepo.save(
                StockTransaction.builder()
                        .product(inv.getProduct())
                        .type(StockType.OUT)
                        .quantity(qty)
                        .note("Xuất kho")
                        .build()
        );
    }
    public List<ProductInventoryView> inventoryView(){

        List<Object[]> rows = inventoryRepo.fetchInventoryView();

        return rows.stream().map(r -> {

            Long productId = ((Number) r[0]).longValue();
            String name = (String) r[1];
            Integer gram = ((Number) r[2]).intValue();
            Double price = ((Number) r[3]).doubleValue();
            Integer qty = r[4]==null?0:((Number) r[4]).intValue();

            LocalDateTime createdAt =
                    r[5]==null?null:((Timestamp) r[5]).toLocalDateTime();

            Integer expiryDays =
                    r[6]==null?0:((Number) r[6]).intValue();

            LocalDateTime expiredAt =
                    r[7]==null?null:((Timestamp) r[7]).toLocalDateTime();

            Long daysLeft =
                    r[8]==null?0L:((Number) r[8]).longValue();

            return new ProductInventoryView(
                    productId,
                    name,
                    gram,
                    price,
                    qty,
                    createdAt,
                    expiryDays,
                    expiredAt,
                    daysLeft
            );

        }).toList();
    }


}
