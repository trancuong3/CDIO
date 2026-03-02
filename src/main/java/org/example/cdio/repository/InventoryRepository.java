package org.example.cdio.repository;

import org.example.cdio.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {

    Optional<Inventory> findByProductId(Long productId);

    @Query(value = """

SELECT
    p.id,
    p.name,
    p.weight_grams,
    p.wholesale_price,
    IFNULL(i.quantity, 0),

    MAX(st.trans_at),

    p.expiry_days,

    CASE 
        WHEN MAX(st.trans_at) IS NULL THEN NULL
        ELSE DATE_ADD(MAX(st.trans_at), INTERVAL p.expiry_days DAY)
    END,

    CASE 
        WHEN MAX(st.trans_at) IS NULL THEN NULL
        ELSE DATEDIFF(
            DATE_ADD(MAX(st.trans_at), INTERVAL p.expiry_days DAY),
            NOW()
        )
    END

FROM products p
LEFT JOIN inventory i ON p.id = i.product_id
LEFT JOIN stock_transactions st
       ON p.id = st.product_id
       AND st.type='IN'
WHERE p.is_active = true
GROUP BY p.id

""", nativeQuery = true)
    List<Object[]> fetchInventoryView();


}
