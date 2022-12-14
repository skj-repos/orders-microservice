package com.classpathio.order.service;

import java.util.Set;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.classpathio.order.model.Order;
import com.classpathio.order.repository.OrderJpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
	
	private final OrderJpaRepository orderRepository;
	private final WebClient webClient;
	private final ApplicationEventPublisher applicationEvent;
	
	@CircuitBreaker(name="inventoryservice", fallbackMethod = "fallback")
	@Retry(name="retryConfig")
	public Order saveOrder(Order order) {
		Order savedOrder = this.orderRepository.save(order);
		log.info("Calling the inventory microservice :: ");
		//make the rest call and update the inventory
		long orderCount = this.webClient
								.post()
								.uri("/api/inventory")
								.exchangeToMono(res -> res.bodyToMono(Long.class))
								.block();
		log.info("Response from inventory microservice :: {}", orderCount);
		return savedOrder;
	}
	
	private Order fallback(Throwable exception) {
		log.error("Exception while making a POST request :: {}", exception.getMessage());
		AvailabilityChangeEvent.publish(applicationEvent, "inventory-service is not operational", ReadinessState.REFUSING_TRAFFIC);
		return Order.builder().build();
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
