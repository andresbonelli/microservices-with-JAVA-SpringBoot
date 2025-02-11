package com.andresbonelli.microservices.order.service;

import com.andresbonelli.microservices.order.client.InventoryClient;
import com.andresbonelli.microservices.order.controller.dto.CreateOrderDTO;
import com.andresbonelli.microservices.order.controller.dto.ResponseOrderDTO;
import com.andresbonelli.microservices.order.controller.dto.UpdateOrderDTO;
import com.andresbonelli.microservices.order.event.OrderPlacedEvent;
import com.andresbonelli.microservices.order.model.Order;
import com.andresbonelli.microservices.order.repository.OrderRepository;
import com.andresbonelli.microservices.order.service.exceptions.OrderNotFoundException;
import com.andresbonelli.microservices.order.service.exceptions.ProductNotInStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public ResponseOrderDTO placeOrder(CreateOrderDTO request) {
        var isProductInStock = inventoryClient.isInStock(request.skuCode(), request.quantity());
        if (!isProductInStock) {
            throw new ProductNotInStockException(String.format("Product %s is not in stock", request.skuCode()));
        }

        Order order = buildOrder(request);
        Order savedOrder = orderRepository.save(order);
        //send notification message

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                savedOrder.getOrderCode(),
                request.userDetails().email()
        );
        log.info("START - Order placed event: {}", orderPlacedEvent);
        kafkaTemplate.send("order-placed", orderPlacedEvent);
        log.info("END - Order placed event: {}", orderPlacedEvent);
        return new ResponseOrderDTO(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getSkuCode(),
                savedOrder.getPrice(),
                savedOrder.getQuantity()
        );
    }

    public List<ResponseOrderDTO> getAll() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> new ResponseOrderDTO(
                order.getId(),
                order.getOrderCode(),
                order.getSkuCode(),
                order.getPrice(),
                order.getQuantity()
        )).toList();
    }

    private Order buildOrder(CreateOrderDTO request) {
        return Order.builder()
                .orderCode(UUID.randomUUID().toString())
                .skuCode(request.skuCode())
                .price(request.price())
                .quantity(request.quantity())
                .build();
    }

    public ResponseOrderDTO getById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        return new ResponseOrderDTO(
                order.getId(),
                order.getOrderCode(),
                order.getSkuCode(),
                order.getPrice(),
                order.getQuantity()
        );
    }

    public ResponseOrderDTO updateOrder(UpdateOrderDTO request) {
        Order order = orderRepository.findById(request.id()).orElseThrow(OrderNotFoundException::new);
        order.setSkuCode(request.skuCode());
        order.setPrice(request.price());
        order.setQuantity(request.quantity());
        Order updatedOrder = orderRepository.save(order);
        return new ResponseOrderDTO(
                updatedOrder.getId(),
                updatedOrder.getOrderCode(),
                updatedOrder.getSkuCode(),
                updatedOrder.getPrice(),
                updatedOrder.getQuantity()
        );
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
