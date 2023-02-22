/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.commerce.inventory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import org.jmolecules.event.types.DomainEvent;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.Quantity;

/**
 * Events published by the inventory module.
 *
 * @author Oliver Drotbohm
 */
public class InventoryEvents {

	/**
	 * Event being thrown if the stock for particular {@link Product} falls below the threshold configured in
	 * {@link InventoryProperties}. No record as we want to hide the constructor.
	 *
	 * @author Oliver Drotbohm
	 */
	@Value
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class StockShort implements DomainEvent {

		ProductIdentifier productId;
		Quantity currentQuantity, threshold;

		static StockShort of(InventoryItem item, Quantity quantity) {
			return new StockShort(item.getProductAssociation().getId(), item.getQuantity(), quantity);
		}
	}

	/**
	 * Event published to signal a change in the quantity of an {@link InventoryItem}.
	 *
	 * @author Oliver Drotbohm
	 */
	@Value(staticConstructor = "of")
	static class QuantityReduced implements DomainEvent {
		InventoryItem item;
	}
}
