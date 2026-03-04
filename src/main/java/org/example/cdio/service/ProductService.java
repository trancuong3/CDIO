package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Inventory;
import org.example.cdio.entity.Product;
import org.example.cdio.repository.InventoryRepository;
import org.example.cdio.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    public void save(Product product) {

        // ===== UPDATE =====
        if (product.getId() != null) {

            Product existing = productRepo.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // 🔥 Giữ nguyên createdAt
            product.setCreatedAt(existing.getCreatedAt());
        }

        Product saved = productRepo.save(product);

        // Nếu chưa có inventory thì tạo mới
        inventoryRepo.findByProductId(saved.getId())
                .orElseGet(() -> {
                    Inventory inv = Inventory.builder()
                            .product(saved)
                            .quantity(0)
                            .minLevel(5)
                            .build();
                    return inventoryRepo.save(inv);
                });
    }

    public Product findById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    // Soft delete
    public void delete(Long id) {

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        product.setIsActive(false);
        productRepo.save(product);
    }
}