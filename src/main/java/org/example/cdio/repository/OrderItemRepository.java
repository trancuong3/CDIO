package org.example.cdio.repository;

import org.example.cdio.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    @Query("""
	    select oi
	    from OrderItem oi
	    join fetch oi.order o
	    join fetch oi.product p
	    left join fetch o.store s
	    order by o.id desc, oi.id asc
	    """)
    List<OrderItem> findAllWithDetails();

    @Query("""
	    select oi
	    from OrderItem oi
	    join fetch oi.order o
	    join fetch oi.product p
	    left join fetch o.store s
	    where o.id = :orderId
	    order by oi.id asc
	    """)
    List<OrderItem> findByOrderIdWithDetails(@Param("orderId") Long orderId);
}
