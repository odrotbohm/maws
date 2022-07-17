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

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.money.MonetaryAmount;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.core.Quantity;
import com.acme.commerce.customer.Customer;
import com.acme.commerce.customer.Customer.CustomerIdentifier;
import com.acme.commerce.order.Order.OrderIdentifier;
import com.acme.commerce.order.OrderEvents.OrderCanceled;
import com.acme.commerce.order.OrderEvents.OrderCompleted;
import com.acme.commerce.order.OrderEvents.OrderPaid;

/**
 * @author Oliver Drotbohm
 */
@Table(name = "ORDERS")
@ToString(doNotUseGetters = true)
public class Order extends AbstractAggregateRoot<Order> implements AggregateRoot<Order, OrderIdentifier> {

	private final @Getter OrderIdentifier id = new OrderIdentifier(UUID.randomUUID());
	private final @Getter Association<Customer, CustomerIdentifier> customer;

	@Getter //
	@Setter(AccessLevel.PACKAGE) //
	private LocalDateTime dateCreated = null;

	@Getter @Enumerated(EnumType.STRING) //
	private OrderStatus orderStatus = OrderStatus.OPEN;

	private List<LineItem> lineItems = new ArrayList<>();

	/**
	 * Creates a new Order
	 *
	 * @param userAccount The {@link UserAccount} connected to this order, must not be {@literal null}.
	 */
	public Order(CustomerIdentifier customerIdentifier) {

		Assert.notNull(customerIdentifier, "CustomerIdentifier must not be null");

		this.customer = Association.forId(customerIdentifier);
		this.dateCreated = LocalDateTime.now();
	}

	/**
	 * Returns all {@link LineItem}s of the {@link Order}.
	 *
	 * @return
	 */
	public Totalable<LineItem> getOrderLines() {
		return Totalable.of(lineItems);
	}

	/**
	 * Returns all {@link LineItem} instances that refer to the given {@link Product}.
	 *
	 * @param product must not be {@literal null}.
	 * @return
	 */
	public Totalable<LineItem> getOrderLines(Product product) {

		Assert.notNull(product, "Product must not be null!");

		return Totalable.of(Streamable.of(() -> lineItems.stream() //
				.filter(it -> it.refersTo(product))));
	}

	/**
	 * Returns the total price of the {@link Order}.
	 *
	 * @return
	 */
	public MonetaryAmount getTotal() {
		return getOrderLines().getTotal();
	}

	/**
	 * Adds an {@link LineItem} to the {@link Order}, the {@link OrderStatus} must be OPEN.
	 *
	 * @param orderLine the {@link LineItem} to be added.
	 * @return the {@link LineItem} added.
	 * @throws IllegalArgumentException if orderLine is {@literal null}.
	 */
	@Deprecated
	LineItem add(LineItem orderLine) {

		Assert.notNull(orderLine, "OrderLine must not be null!");
		assertOrderIsOpen();

		this.lineItems.add(orderLine);

		return orderLine;
	}

	/**
	 * Adds an {@link LineItem} for the given with the given {@link Quantity}.
	 *
	 * @param product must not be {@literal null}.
	 * @param quantity must not be {@literal null}.
	 * @return the {@link LineItem} added.
	 */
	public LineItem addOrderLine(Product product, Quantity quantity) {

		Assert.notNull(product, "Product must not be null!");
		Assert.notNull(quantity, "Quantity must not be null!");

		LineItem orderLine = new LineItem(product, quantity);

		this.lineItems.add(orderLine);

		return orderLine;
	}

	/**
	 * Removes the given {@link LineItem} as well as all {@link AttachedChargeLine} associated with it.
	 *
	 * @param orderLine must not be {@literal null}.
	 */
	public void remove(LineItem orderLine) {

		Assert.notNull(orderLine, "OrderLine must not be null!");
		assertOrderIsOpen();

		this.lineItems.remove(orderLine);
	}

	/**
	 * Convenience method for checking if an order has the status PAID
	 *
	 * @return true if OrderStatus is PAID, otherwise false
	 */
	public boolean isPaid() {
		return orderStatus == OrderStatus.PAID;
	}

	/**
	 * Convenience method for checking if an order has the status CANCELLED
	 *
	 * @return true if OrderStatus is CANCELLED, otherwise false
	 */
	public boolean isCanceled() {
		return orderStatus == OrderStatus.CANCELLED;
	}

	/**
	 * Convenience method for checking if an order has the status COMPLETED
	 *
	 * @return true if OrderStatus is COMPLETED, otherwise false
	 */
	public boolean isCompleted() {
		return orderStatus == OrderStatus.COMPLETED;
	}

	/**
	 * Convenience method for checking if an order has the status OPEN
	 *
	 * @return true if OrderStatus is OPEN, otherwise false
	 */
	public boolean isOpen() {
		return orderStatus == OrderStatus.OPEN;
	}

	Order complete() {

		Assert.isTrue(isPaid(), "An order must be paid to be completed!");

		this.orderStatus = OrderStatus.COMPLETED;

		registerEvent(OrderCompleted.of(this));

		return this;
	}

	/**
	 * Cancels the current {@link Order} with the given reason. Will publish an {@link OrderCanceled} even
	 *
	 * @param reason must not be {@literal null}.
	 * @return
	 */
	Order cancel(String reason) {

		Assert.isTrue(!isCanceled(), "Order is already cancelled!");

		if (!isCompleted()) {
			registerEvent(OrderCompleted.of(this));
		}

		this.orderStatus = OrderStatus.CANCELLED;

		registerEvent(OrderCanceled.of(this, reason));

		return this;
	}

	int getNumberOfLineItems() {
		return this.lineItems.size();
	}

	boolean isPaymentExpected() {
		return orderStatus == OrderStatus.OPEN;
	}

	Order markPaid() {

		Assert.isTrue(!isPaid(), "Order is already paid!");

		this.orderStatus = OrderStatus.PAID;

		registerEvent(OrderPaid.of(this));

		return this;
	}

	/**
	 * Asserts that the {@link Order} is {@link OrderStatus#OPEN}. Usually a precondition to manipulate the {@link Order}
	 * state internally.
	 */
	private void assertOrderIsOpen() {

		if (!isOpen()) {
			throw new IllegalStateException("Order is not open anymore! Current state is: " + orderStatus);
		}
	}

	@Value
	public final class OrderIdentifier implements Identifier {
		UUID id;
	}
}
