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

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.acme.commerce.catalog.Product.ProductAdded;
import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.inventory.InventoryItem.InventoryItemAdded;
import com.acme.commerce.order.OrderManagement;

/**
 * @author Oliver Drotbohm
 */
@ApplicationModuleTest
@RequiredArgsConstructor
class InventoryModuleTests {

	private final Inventory inventory;

	@MockBean OrderManagement orders;

	@Test
	void createsInventoryItemOnProductAddition(Scenario scenario) throws Exception {

		var productId = new ProductIdentifier(UUID.randomUUID());

		scenario.publish(new ProductAdded(productId))
				.andWaitAtMost(Duration.ofSeconds(2)) // optional
				.forEventOfType(InventoryItemAdded.class)
				.toArriveAndVerify(it -> {

					assertThat(inventory.findByProductIdentifier(productId)).isPresent();
					assertThat(inventory.findById(it.id())).isPresent();
				});
	}
}
