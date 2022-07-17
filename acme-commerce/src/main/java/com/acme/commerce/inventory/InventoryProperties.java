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

import lombok.Value;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import com.acme.commerce.core.Quantity;

/**
 * Properties to configure Salespoint's inventory. Declare {@code salespoint.inventory.…} in application properties to
 * tweak settings.
 *
 * @author Oliver Drotbohm
 */
@Value
@ConfigurationProperties("acme.commerce.inventory")
class InventoryProperties {

	/**
	 * The threshold at which a {@link InventoryEvents.StockShort} is supposed to be triggered during inventory updates.
	 */
	private Quantity restockThreshold;

	InventoryProperties(@Nullable Quantity restockThreshold) {
		this.restockThreshold = restockThreshold == null ? Quantity.NONE : restockThreshold;
	}
}
