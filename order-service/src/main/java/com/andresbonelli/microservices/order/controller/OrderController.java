package com.andresbonelli.microservices.order.controller;

import com.andresbonelli.microservices.order.controller.dto.CreateOrderDTO;
import com.andresbonelli.microservices.order.controller.dto.ResponseOrderDTO;
import com.andresbonelli.microservices.order.controller.dto.UpdateOrderDTO;
import com.andresbonelli.microservices.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseOrderDTO placeOrder(@RequestBody CreateOrderDTO request) {
        return orderService.placeOrder(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseOrderDTO> getALlOrders() {
        return orderService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseOrderDTO getOrderById(@PathVariable("id") Long id) {
        return orderService.getById(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseOrderDTO updateOrder(@RequestBody UpdateOrderDTO request) {
        return orderService.updateOrder(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
    }
}
