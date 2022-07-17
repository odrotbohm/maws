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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jmolecules.event.annotation.DomainEventHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.moments.DayHasPassed;
import org.springframework.stereotype.Component;

import com.acme.commerce.catalog.Product;
import com.acme.commerce.inventory.InventoryEvents.QuantityReduced;
import com.acme.commerce.inventory.InventoryEvents.StockShort;
import com.acme.commerce.order.LineItem;
import com.acme.commerce.order.Order;
import com.acme.commerce.order.OrderCompletionFailure;
import com.acme.commerce.order.OrderEvents.OrderCanceled;
import com.acme.commerce.order.OrderEvents.OrderCompleted;

/**
 * Event listeners of the inventory module.
 *
 * @author Oliver Drotbohm
 */
class InventoryListeners {

	/**
	 * Event listener to monitor {@link QuantityReduced} events and compare against the
	 * {@link InventoryProperties#getRestockThreshold()} configured.
	 *
	 * @author Oliver Drotbohm
	 */
	@Slf4j
	@Component
	@RequiredArgsConstructor
	static class InternalInventoryListeners {

		private final @NonNull InventoryProperties configuration;
		private final @NonNull Inventory inventory;

		@DomainEventHandler
		StockShort on(QuantityReduced event) {

			var threshold = configuration.getRestockThreshold();
			var item = event.getItem();

			return item.hasSufficientQuantity(threshold)
					? null
					: StockShort.of(item, threshold);
		}

		@DomainEventHandler
		void on(DayHasPassed event) {

			var outOfStock = inventory.findItemsOutOfStock();

			if (outOfStock.isEmpty()) {
				return;
			}

			log.info("Items out of stock on {}:", event.getDate());

			outOfStock.map(Object::toString).forEach(log::info);
		}
	}

	/**
	 * {@link ApplicationListener} for {@link OrderCompleted} events to verify that sufficient amounts of the
	 * {@link Product} the {@link LineItem}s contained in the {@link Order} point to are available in the
	 * {@link UniqueInventory}.
	 *
	 * @author Oliver Drotbohm
	 */
	@Component
	@RequiredArgsConstructor
	@ConditionalOnProperty(name = "salespoint.inventory.disable-updates", havingValue = "false", matchIfMissing = true)
	static class InventoryOrderEventListener {

		private final @NonNull InventoryManagement management;

		/**
		 * Invokes {@link UniqueInventory} checks for all {@link LineItem} of the {@link Order} in the given
		 * {@link OrderCompleted} event.
		 *
		 * @param event must not be {@literal null}.
		 * @throws OrderCompletionFailure in case any of the {@link LineItem} items contained in the order and supported by
		 *           the configured {@link LineItemFilter} is not available in sufficient quantity.
		 */
		@EventListener
		public void on(OrderCompleted event) throws OrderCompletionFailure {
			management.verifyAndUpdate(event.getOrder());
		}

		/**
		 * Rolls back the stock decreases handled for {@link OrderCompleted} events.
		 *
		 * @param event must not be {@literal null}.
		 */
		@EventListener
		public void on(OrderCanceled event) {
			management.cancelOrder(event.getOrder());
		}
	}
}
