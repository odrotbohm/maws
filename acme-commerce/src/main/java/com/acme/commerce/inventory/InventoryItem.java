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
package com.acme.commerce.inventory;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.Quantity;
import com.acme.commerce.inventory.InventoryEvents.QuantityReduced;

/**
 * An {@link InventoryItem} associates a product with a {@link Quantity} to keep track of how many items per product are
 * available.
 *
 * @author Oliver Drotbohm
 */
@EntityListeners(InventoryItemCreationListener.class)
@Getter
public class InventoryItem extends AbstractAggregateRoot<InventoryItem>
		implements AggregateRoot<InventoryItem, Identifier> {

	private final InventoryItemIdentifier inventoryItemIdentifier = new InventoryItemIdentifier(
			UUID.randomUUID());

	private final Association<Product, ProductIdentifier> productAssociation;

	private Quantity quantity;

	/**
	 * Creates a new {@link InventoryItem} for the given {@link Product} and {@link Quantity}.
	 *
	 * @param quantity the initial {@link Quantity} for this {@link InventoryItem}, must not be {@literal null}.
	 */
	protected InventoryItem(ProductIdentifier productIdentifier, Quantity quantity) {

		Assert.notNull(productIdentifier, "Product must be not null!");
		Assert.notNull(quantity, "Quantity must be not null!");

		this.quantity = quantity;
		this.productAssociation = Association.forId(productIdentifier);
	}

	public final InventoryItemIdentifier getId() {
		return inventoryItemIdentifier;
	}

	/**
	 * Returns whether the {@link InventoryItem} is available in exactly or more of the given quantity.
	 *
	 * @param quantity must not be {@literal null}.
	 * @return
	 */
	public boolean hasSufficientQuantity(Quantity quantity) {
		return !this.quantity.subtract(quantity).isNegative();
	}

	/**
	 * Decreases the quantity of the current {@link InventoryItem} by the given {@link Quantity}.
	 *
	 * @param quantity must not be {@literal null}.
	 */
	public InventoryItem decreaseQuantity(Quantity quantity) {

		Assert.notNull(quantity, "Quantity must not be null!");
		Assert.isTrue(this.quantity.isGreaterThanOrEqualTo(quantity),
				String.format("Insufficient quantity! Have %s but was requested to reduce by %s.", this.quantity, quantity));

		// getProduct().verify(quantity);

		this.quantity = this.quantity.subtract(quantity);

		registerEvent(QuantityReduced.of(this));

		return this;
	}

	/**
	 * Increases the quantity of the current {@link InventoryItem} by the given {@link Quantity}.
	 *
	 * @param quantity must not be {@literal null}.
	 */
	public InventoryItem increaseQuantity(Quantity quantity) {

		Assert.notNull(quantity, "Quantity must not be null!");

		this.quantity = this.quantity.add(quantity);

		return this;
	}

	/**
	 * Returns whether the {@link InventoryItem} belongs to the given {@link Product}.
	 *
	 * @param product must not be {@literal null}.
	 * @return
	 */
	public boolean keepsTrackOf(Product product) {
		return this.productAssociation.pointsTo(product);
	}

	/**
	 * Returns whether the given {@link InventoryItem} is a different one but keeping track of the same {@link Product}.
	 *
	 * @param other
	 * @return
	 */
	boolean isDifferentItemForSameProduct(InventoryItem other) {
		return !this.equals(other) && this.productAssociation.pointsToSameAggregateAs(other.productAssociation);
	}

	/**
	 * Manual verification that invariants are met as JPA requires us to expose a default constructor that also needs to
	 * be callable from sub-classes as they need to declare one as well.
	 */
	@PrePersist
	void verifyConstraints() {

		Assert.state(quantity != null,
				"No quantity set! Make sure you have created the product by calling a non-default constructor!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return String.format("%s(%s) for Product(%s) with quantity %s", //
				getClass().getSimpleName(), getId(), productAssociation.getId(), getQuantity());
	}

	@Value
	public final class InventoryItemIdentifier implements Identifier {
		UUID id;
	}
}
