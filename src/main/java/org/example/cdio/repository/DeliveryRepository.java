package org.example.cdio.repository;

import org.example.cdio.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
    List<Delivery> findByDelivererNameOrderByShippedAtDesc(String delivererName);
    Optional<Delivery> findByIdAndDelivererName(Long id, String delivererName);
}
