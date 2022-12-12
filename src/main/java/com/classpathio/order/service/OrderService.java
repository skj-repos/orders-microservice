package com.classpathio.order.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.classpathio.order.model.Order;
import com.classpathio.order.repository.OrderJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	
	private final OrderJpaRepository orderRepository;
	
	public Order saveOrder(Order order) {
		return this.orderRepository.save(order);
	}
	
	public Set<Order> fetchOrders(){
		return Set.copyOf(orderRepository.findAll());
	}
	
	public Order fetchOrderById(long id) {
		return this.orderRepository
							.findById(id)
							.orElseThrow(() -> new IllegalArgumentException("invalid order id"));
	}
	
	public void deleteOrderById(long id) {
		this.orderRepository.deleteById(id);
	}

}
