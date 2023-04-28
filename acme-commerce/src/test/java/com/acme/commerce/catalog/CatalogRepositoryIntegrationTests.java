/*
 * Copyright 2023 the original author or authors.
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
package com.acme.commerce.catalog;

import static org.assertj.core.api.Assertions.*;

import lombok.RequiredArgsConstructor;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.acme.commerce.catalog.Product.ProductAdded;
import com.acme.commerce.core.Currencies;

/**
 * Integration tests for {@link Catalog}.
 *
 * @author Oliver Drotbohm
 */
@ApplicationModuleTest
@RequiredArgsConstructor
class CatalogRepositoryIntegrationTests {

	final Catalog catalog;

	@Test
	void addingAProductPublishesEvent(Scenario scenario) {

		scenario.stimulate(() -> catalog.save(new Product("Some product.", Money.of(30, Currencies.EURO))))
				.andCleanup(catalog::delete)
				.andWaitForEventOfType(ProductAdded.class)
				.toArriveAndVerify((__, it) -> {
					assertThat(catalog.findById(it.getId())).isPresent();
				});
	}
}
