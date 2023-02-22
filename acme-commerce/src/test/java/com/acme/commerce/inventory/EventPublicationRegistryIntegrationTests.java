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

import static org.assertj.core.api.Assertions.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.modulith.events.EventPublicationRegistry;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.Quantity;
import com.acme.commerce.inventory.EventPublicationRegistryIntegrationTests.SomeTestEventListener;
import com.acme.commerce.inventory.InventoryEvents.StockShort;
import com.acme.commerce.order.OrderManagement;

/**
 * @author Oliver Drotbohm
 */
@ApplicationModuleTest
@Import(SomeTestEventListener.class)
@RequiredArgsConstructor
class EventPublicationRegistryIntegrationTests {

	private final SomeTestEventListener listener;
	private final EventPublicationRegistry registry;

	@MockBean OrderManagement orders;

	@Test
	void logsFailedEventPublicationsOnClose(Scenario scenario) {

		var productIdentifier = new ProductIdentifier(UUID.randomUUID());
		var item = new InventoryItem(productIdentifier, Quantity.of(10));

		scenario.publish(StockShort.of(item, Quantity.of(15)))
				.andWaitForStateChange(() -> listener.getEx())
				.andVerify(__ -> {
					assertThat(registry.findIncompletePublications()).hasSize(1);
				});

	}

	static class SomeTestEventListener {

		private @Getter RuntimeException ex;

		@ApplicationModuleListener
		void on(StockShort event) throws Exception {

			Thread.sleep(1000);

			var ex = new IllegalStateException("¯\\_(ツ)_/¯");

			try {
				throw ex;
			} finally {
				this.ex = ex;
			}
		}
	}
}
