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

import static com.acme.commerce.order.OrderCompletionReport.OrderLineCompletion.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

import org.springframework.data.util.Optionals;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.acme.commerce.order.LineItem;
import com.acme.commerce.order.Order;
import com.acme.commerce.order.OrderCompletionFailure;
import com.acme.commerce.order.OrderCompletionReport;
import com.acme.commerce.order.OrderCompletionReport.OrderLineCompletion;

/**
 * @author Oliver Drotbohm
 */
@Service
@RequiredArgsConstructor
class InventoryManagement {

	private static final String NOT_ENOUGH_STOCK = "Number of items requested by the OrderLine is greater than the number available in the Inventory. Please re-stock.";
	private static final String NO_INVENTORY_ITEM = "No inventory item with given product indentifier found in inventory. Have you initialized your inventory? Do you need to re-stock it?";

	private final @NonNull Inventory inventory;

	public OrderCompletionReport verifyAndUpdate(Order order) {

		var collect = order.getOrderLines() //
				.map(this::verify)//
				.toList();

		return OrderCompletionReport.forCompletions(order, collect) //
				.onError(OrderCompletionFailure::new);
	}

	public void cancelOrder(Order order) {

		if (!order.isCompleted()) {
			return;
		}

		order.getOrderLines() //
				.flatMap(this::updateStockFor) //
				.forEach(inventory::save);
	}

	/**
	 * Verifies the given {@link LineItem} for sufficient stock in the {@link UniqueInventory}.
	 *
	 * @param orderLine must not be {@literal null}.
	 * @return
	 */
	private OrderLineCompletion verify(LineItem orderLine) {

		Assert.notNull(orderLine, "OrderLine must not be null!");

		var identifier = orderLine.getProductIdentifier();
		var item = inventory.findByProductIdentifier(identifier.getId());

		return item.map(it -> verifyAndUpdateUnique(it, orderLine)) //
				.orElseGet(() -> assertAtLeastOneExists(orderLine));
	}

	/**
	 * Verifies that the given UI
	 *
	 * @param item
	 * @param orderLine
	 * @return
	 */
	private OrderLineCompletion verifyAndUpdateUnique(InventoryItem item, LineItem orderLine) {
		return hasSufficientQuantity(item, orderLine)
				.onSuccess(it -> inventory.save(item.decreaseQuantity(it.getQuantity())));
	}

	/**
	 * Creates a new {@link OrderLineCompletion} verifying that at least one {@link MultiInventoryItem} exists.
	 *
	 * @param orderLine must not be {@literal null}.
	 * @return
	 */
	private OrderLineCompletion assertAtLeastOneExists(LineItem orderLine) {

		var items = inventory.findByProductIdentifier(orderLine.getProductIdentifier().getId());

		return items.isEmpty() ? error(orderLine, NO_INVENTORY_ITEM) : skipped(orderLine);
	}

	private Stream<InventoryItem> updateStockFor(LineItem orderLine) {

		var productIdentifier = orderLine.getProductIdentifier();
		var item = inventory.findByProductIdentifier(productIdentifier.getId())
				.map(it -> it.increaseQuantity(orderLine.getQuantity()));

		if (!item.isPresent() && inventory.findByProductIdentifier(productIdentifier.getId()).isEmpty()) {
			throw new IllegalArgumentException(
					"Couldn't find InventoryItem for product %s!".formatted(productIdentifier));
		}

		return Optionals.toStream(item);
	}

	private static OrderLineCompletion hasSufficientQuantity(InventoryItem item, LineItem orderLine) {

		return item.hasSufficientQuantity(orderLine.getQuantity()) //
				? success(orderLine) //
				: error(orderLine, NOT_ENOUGH_STOCK);
	}
}
