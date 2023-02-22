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

import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

import javax.money.MonetaryAmount;

import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.util.Assert;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.MetricMismatchException;
import com.acme.commerce.core.Quantity;
import com.acme.commerce.order.LineItem.LineItemIdentifier;

/**
 * An order line represents the price and the {@link Quantity} of a {@link Product} that is intended to be purchased as
 * part of an {@link Order}.
 * <p>
 * Order lines should not be used to represent expenses for services, such as shipping. For this purpose,
 * {@link ChargeLine} should be used instead.
 * <p>
 * Note that the constructor of this class creates a copy of the product's name and price, so that changes to those
 * attributes do not affect existing orders.
 *
 * @author Oliver Drotbohm
 */
@ToString
@Getter
public class LineItem implements Entity<Order, LineItemIdentifier>, Priced {

	private final LineItemIdentifier id = new LineItemIdentifier(UUID.randomUUID());
	private final Association<Product, ProductIdentifier> productIdentifier;

	private MonetaryAmount price;
	private Quantity quantity;
	private String productName;

	/**
	 * Creates a new {@link LineItem} for the given {@link Product} and {@link Quantity}.
	 *
	 * @param product must not be {@literal null}.
	 * @param quantity must not be {@literal null}.
	 */
	LineItem(Product product, Quantity quantity) {

		Assert.notNull(product, "Product must be not null!");
		Assert.notNull(quantity, "Quantity must be not null!");

		if (!product.supports(quantity)) {
			throw new MetricMismatchException("Product %s does not support quantity %s!".formatted(product, quantity));
		}

		this.productIdentifier = Association.forId(product.getId());
		this.quantity = quantity;
		this.price = product.getPrice().multiply(quantity.getAmount());
		this.productName = product.getName();
	}

	/**
	 * Returns whether the {@link LineItem} refers to the given {@link Product}.
	 *
	 * @param product must not be {@literal null}.
	 * @return
	 */
	public boolean refersTo(Product product) {

		Assert.notNull(product, "Product must not be null!");

		return this.productIdentifier.pointsTo(product);
	}

	/**
	 * Returns whether the {@link LineItem} refers to the {@link Product} with the given identifier.
	 *
	 * @param identifier must not be {@literal null}.
	 * @return
	 */
	public boolean refersTo(ProductIdentifier identifier) {

		Assert.notNull(identifier, "Product identifier must not be null!");

		return this.productIdentifier.pointsTo(identifier);
	}

	public record LineItemIdentifier(UUID id) implements Identifier {}
}
