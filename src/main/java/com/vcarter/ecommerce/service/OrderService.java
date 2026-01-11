package com.vcarter.ecommerce.service;

import com.vcarter.ecommerce.entity.Order;
import com.vcarter.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository repo;

    public List<Order> getAllOrders() { return repo.findAll(); }

    public List<Order> getOrdersByCustomerId(Long customerId) { return repo.findByCustomer_Id(customerId); }

    public Order getOrderById(Long orderId) { return repo.findById(orderId).orElse(null); }

    public Order saveOrder(Order order) { return repo.save(order); }

    public boolean deleteOrder(Long orderId) {
        if (repo.existsById(orderId)) {
            repo.deleteById(orderId);
            return true;
        }
        return false;
    }
}
