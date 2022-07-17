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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;

import org.springframework.data.util.Streamable;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.core.Quantity;

/**
 * An abstraction over a collection of {@link InventoryItem}s. Offers convenience methods to handle cases in which a
 * lookup of {@link InventoryItem}s for a {@link Product} return multiple items or a {@link UniqueInventoryItem}.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor(staticName = "of")
public class InventoryItems implements Streamable<InventoryItem> {

	private final @NonNull Streamable<InventoryItem> items;

	/**
	 * Returns the total quantity of all the {@link InventoryItem}s contained.
	 *
	 * @return will never be {@literal null}.
	 */
	public Quantity getTotalQuantity() {

		return stream() //
				.map(InventoryItem::getQuantity) //
				.reduce(Quantity::add) //
				.orElse(Quantity.NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<InventoryItem> iterator() {
		return items.iterator();
	}
}
