package org.example.cdio.repository;

import org.example.cdio.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
	List<Delivery> findByDeliveredByOrderByCreatedAtDesc(Long deliveredBy);
	List<Delivery> findByOrderIdAndDeliveredByOrderByIdAsc(Long orderId, Long deliveredBy);
	Optional<Delivery> findFirstByOrderIdAndDeliveredByOrderByCreatedAtDesc(Long orderId, Long deliveredBy);
	List<Delivery> findByDeliveredByAndMaDonHangIsNotNullOrderByCreatedAtDesc(Long deliveredBy);
	Optional<Delivery> findFirstByMaDonHangAndDeliveredByOrderByCreatedAtDesc(String maDonHang, Long deliveredBy);
	Optional<Delivery> findFirstByMaDonHangOrderByCreatedAtDesc(String maDonHang);
	Optional<Delivery> findByIdAndDeliveredBy(Long id, Long deliveredBy);
}
