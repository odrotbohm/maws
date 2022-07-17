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

import java.util.Optional;

import org.jmolecules.ddd.types.Association;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.Quantity;
import com.acme.commerce.inventory.InventoryItem.InventoryItemIdentifier;

/**
 * Base interface for {@link InventoryItem} implementations. Choose either {@link UniqueInventory} or
 * {@link MultiInventory} for your application depending on whether you need to keep track of multiple locations (e.g.
 * warehouses) in which {@link InventoryItem}s are supposed to be managed.
 * <p>
 * {@link UniqueInventoryItem} expects a one-to-one relationship to a product, which is equivalent to modeling a single
 * warehouse. That's simple and allows to look up the {@link UniqueInventoryItem} by {@link ProductIdentifier}. I.e. the
 * {@link Quantity} contained in that {@link UniqueInventoryItem} is equivalent to the overall quantity in the system.
 * This is the simpler model in general and should be preferred. {@link Product}s held in {@link UniqueInventoryItem}s
 * are suspect to automatic inventory updates on order completion. See
 * {@link org.salespointframework.inventory.InventoryListeners.InventoryOrderEventListener} for details.
 * <p>
 * If you absolutely need to model {@link Product}s managed in multiple warehouses, use {@link MultiInventoryItem}
 * alongside {@link MultiInventory}. {@link MultiInventory#findByProductIdentifier(ProductIdentifier)} rather returns an
 * {@link InventoryItems} instance. The overall {@link Quantity} of {@link Product}s in the system can then be obtained
 * via {@link InventoryItems#getTotalQuantity()}. {@link MultiInventoryItem}s are not suspect to auto-inventory updates
 * upon order completion as it's not clear which of the {@link InventoryItem}s is supposed to be deducted.
 *
 * @author Oliver Drotbohm
 */
public interface Inventory extends CrudRepository<InventoryItem, InventoryItemIdentifier> {

	/**
	 * Returns all {@link InventoryItem}s as {@link Streamable} usually to concatenate with other {@link Streamable}s of
	 * {@link InventoryItem}s. For dedicated access to the concrete sub-type of {@link InventoryItem} use
	 * {@link SalespointRepository#findAll()}.
	 *
	 * @return
	 * @see SalespointRepository#findAll()
	 */
	@Query("select i from #{#entityName} i")
	Streamable<InventoryItem> streamAll();

	/**
	 * Returns all {@link UniqueInventoryItem}s that are out of stock (i.e. the {@link Quantity}'s amount is equal or less
	 * than zero).
	 *
	 * @return will never be {@literal null}.
	 */
	@Query("select i from #{#entityName} i where i.quantity.amount <= 0")
	Streamable<InventoryItem> findItemsOutOfStock();

	/**
	 * Returns the {@link InventoryItem} for the given {@link ProductIdentifier}.
	 *
	 * @param productIdentifier must not be {@literal null}.
	 * @return
	 */
	default Optional<InventoryItem> findByProductIdentifier(ProductIdentifier productIdentifier) {
		return findByProductIdentifier(Association.forId(productIdentifier));
	}

	@Query("select i from #{#entityName} i where i.productAssociation = ?1")
	Optional<InventoryItem> findByProductIdentifier(Association<Product, ProductIdentifier> association);

	/**
	 * Returns the {@link InventoryItem} for the given {@link Product}.
	 *
	 * @param product must not be {@literal null}.
	 * @return
	 */
	default Optional<InventoryItem> findByProduct(Product product) {
		return findByProductIdentifier(product.getId());
	}
}
