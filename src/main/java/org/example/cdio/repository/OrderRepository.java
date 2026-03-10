package org.example.cdio.repository;

import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
	List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}
