package org.example.cdio.repository;

import org.example.cdio.entity.Order;
import org.example.cdio.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {

    @Query("""
	    select o
	    from Order o
	    left join fetch o.store s
	    where o.status = :status
	    order by o.createdAt desc
	    """)
    List<Order> findByStatusWithStoreOrderByCreatedAtDesc(@Param("status") OrderStatus status);

    @Query("""
	    select o
	    from Order o
	    left join fetch o.store s
	    where o.createdBy = :shipperId and o.status = :status
	    order by o.createdAt desc
	    """)
    List<Order> findByCreatedByAndStatusWithStoreOrderByCreatedAtDesc(
	    @Param("shipperId") Long shipperId,
	    @Param("status") OrderStatus status
    );

    Optional<Order> findByIdAndCreatedBy(Long id, Long createdBy);
}
