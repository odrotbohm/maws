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
package com.acme.commerce.catalog;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.money.MonetaryAmount;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.Metric;
import com.acme.commerce.core.MetricMismatchException;
import com.acme.commerce.core.Quantity;

/**
 * A product.
 *
 * @author Oliver Drotbohm
 */
public class Product extends AbstractAggregateRoot<Product> implements AggregateRoot<Product, ProductIdentifier> {

	private static final String INVALID_METRIC = "Product %s does not support quantity %s using metric %s!";

	private ProductIdentifier id = new ProductIdentifier(UUID.randomUUID());
	private @Getter @Setter String name;
	private @Getter @Setter MonetaryAmount price;
	private @ElementCollection(fetch = FetchType.EAGER) Set<String> categories = new HashSet<String>();
	private Metric metric;

	/**
	 * Creates a new {@link Product} with the given name and price.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param price must not be {@literal null}.
	 */
	public Product(String name, MonetaryAmount price) {
		this(name, price, Metric.UNIT);
	}

	/**
	 * Creates a new {@link Product} with the given name, price and {@link Metric}.
	 *
	 * @param name the name of the {@link Product}, must not be {@literal null} or empty.
	 * @param price the price of the {@link Product}, must not be {@literal null}.
	 * @param metric the {@link Metric} of the {@link Product}, must not be {@literal null}.
	 */
	public Product(String name, MonetaryAmount price, Metric metric) {

		Assert.hasText(name, "Name must not be null or empty!");
		Assert.notNull(price, "Price must not be null!");
		Assert.notNull(metric, "Metric must not be null!");

		this.name = name;
		this.price = price;
		this.metric = metric;

		registerEvent(new ProductAdded(id));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Persistable#getId()
	 */
	@Override
	public ProductIdentifier getId() {
		return id;
	}

	/**
	 * Returns the categories the {@link Product} is assigned to.
	 *
	 * @return will never be {@literal null}.
	 */
	public Streamable<String> getCategories() {
		return Streamable.of(Collections.unmodifiableSet(categories));
	}

	/**
	 * Adds the {@link Product} to the given category.
	 *
	 * @param category must not be {@literal null} or empty.
	 * @return
	 */
	public final boolean addCategory(String category) {

		Assert.hasText(category, "category must not be null");
		return categories.add(category);
	}

	public final boolean removeCategory(String category) {

		Assert.notNull(category, "category must not be null");
		return categories.remove(category);
	}

	/**
	 * Returns whether the {@link Product} supports the given {@link Quantity}.
	 *
	 * @param quantity
	 * @return
	 */
	public boolean supports(Quantity quantity) {

		Assert.notNull(quantity, "Quantity must not be null!");
		return quantity.isCompatibleWith(metric);
	}

	/**
	 * Verifies the given {@link Quantity} to match the one supported by the current {@link Product}.
	 *
	 * @param quantity
	 * @throws MetricMismatchException in case the {@link Product} does not support the given {@link Quantity}.
	 */
	public void verify(Quantity quantity) {

		if (!supports(quantity)) {
			throw new MetricMismatchException(INVALID_METRIC.formatted(this, quantity, quantity.getMetric()));
		}
	}

	/**
	 * Creates a {@link Quantity} of the given amount and the current {@link Product}'s underlying {@link Metric}.
	 *
	 * @param amount must not be {@literal null}.
	 * @return
	 */
	public Quantity createQuantity(double amount) {
		return Quantity.of(amount, metric);
	}

	/**
	 * Creates a {@link Quantity} of the given amount and the current {@link Product}'s underlying {@link Metric}.
	 *
	 * @param amount must not be {@literal null}.
	 * @return
	 */
	public Quantity createQuantity(long amount) {
		return Quantity.of(amount, metric);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "%s, %s, %s, handled in %s".formatted(name, id, price, metric);
	}

	public static record ProductAdded(ProductIdentifier id) {}

	public record ProductIdentifier(UUID id) implements Identifier {}
}
