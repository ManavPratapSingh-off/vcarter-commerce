package com.vcarter.ecommerce.controller.v1;

import com.vcarter.ecommerce.entity.Order;
import com.vcarter.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private OrderService service;

    @GetMapping
    public ResponseEntity<?> getAllOrders() { return ResponseEntity.ok(service.getAllOrders()); }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<?> getOrdersByCustomerId(Long customerId) { return ResponseEntity.ok(service.getOrdersByCustomerId(customerId)); }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(Long orderId) { return ResponseEntity.ok(service.getOrderById(orderId)); }

    @PostMapping
    public ResponseEntity<?> createOrder(Order order) { return ResponseEntity.status(201).body(service.saveOrder(order)); }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId, @RequestBody Order order) {
        var existingOrder= service.getOrderById(orderId);
        if (existingOrder == null) return ResponseEntity.status(404).body("Order not found");
        return ResponseEntity.ok(service.saveOrder(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        var result=service.deleteOrder(orderId);
        return ResponseEntity.status(204).body(result? "Order deleted successfully" : "Failed to delete order");
    }
}
