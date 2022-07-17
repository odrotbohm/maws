/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.commerce.order;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.acme.commerce.customer.Customer.CustomerIdentifier;
import com.acme.commerce.order.Order.OrderIdentifier;

/**
 * @author Oliver Drotbohm
 */
@Service
@Transactional
@RequiredArgsConstructor
class DefaultOrderManagement implements OrderManagement {

	private final @NonNull OrderRepository orderRepository;

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#save(org.salespointframework.order.Order)
	 */
	@Override
	public Order save(Order order) {

		Assert.notNull(order, "Order must be not null");

		return orderRepository.save(order);
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#get(org.salespointframework.order.OrderIdentifier)
	 */
	@Override
	public Optional<Order> get(OrderIdentifier orderIdentifier) {

		Assert.notNull(orderIdentifier, "orderIdentifier must not be null");

		return orderRepository.findById(orderIdentifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#contains(org.salespointframework.order.OrderIdentifier)
	 */
	@Override
	public boolean contains(OrderIdentifier orderIdentifier) {

		Assert.notNull(orderIdentifier, "OrderIdentifier must not be null");

		return orderRepository.existsById(orderIdentifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#findBy(org.salespointframework.order.OrderStatus)
	 */
	@Override
	public Streamable<Order> findBy(OrderStatus orderStatus) {

		Assert.notNull(orderStatus, "OrderStatus must not be null");

		return orderRepository.findByOrderStatus(orderStatus);
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#findBy(org.salespointframework.useraccount.UserAccount)
	 */
	@Override
	public Streamable<Order> findBy(CustomerIdentifier identifier) {

		Assert.notNull(identifier, "CustomerIdentifier must not be null");

		return orderRepository.findByCustomer(identifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#completeOrder(org.salespointframework.order.Order)
	 */
	@Override
	public void completeOrder(Order order) {

		Assert.notNull(order, "Order must not be null!");

		if (!order.isPaid()) {
			throw new OrderCompletionFailure(order, "Order is not paid yet!");
		}

		save(order.complete());
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#payOrder(org.salespointframework.order.Order)
	 */
	@Override
	public boolean payOrder(Order order) {

		Assert.notNull(order, "Order must not be null");

		if (!order.isPaymentExpected()) {
			return false;
		}

		save(order.markPaid());

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#cancelOrder(org.salespointframework.order.Order, java.lang.String)
	 */
	@Override
	public boolean cancelOrder(Order order, String reason) {

		Assert.notNull(order, "Order must not be null");

		if (!order.isCanceled()) {
			save(order.cancel(reason));
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#delete(org.salespointframework.order.Order)
	 */
	public Order delete(Order order) {

		Assert.notNull(order, "Order must not be null!");

		orderRepository.delete(order);

		return order;
	}

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.order.OrderManagement#findAll(org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<Order> findAll(Pageable pageable) {
		return orderRepository.findAll(pageable);
	}
}
