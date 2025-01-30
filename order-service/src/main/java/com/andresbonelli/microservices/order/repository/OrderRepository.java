package com.andresbonelli.microservices.order.repository;

import com.andresbonelli.microservices.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    public Order findByOrderCode(String orderCode);
}
