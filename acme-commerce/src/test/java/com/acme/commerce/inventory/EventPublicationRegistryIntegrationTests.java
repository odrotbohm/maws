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

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.EventPublicationRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.acme.commerce.AcmeCommerce;
import com.acme.commerce.catalog.Product.ProductIdentifier;
import com.acme.commerce.core.Quantity;
import com.acme.commerce.inventory.InventoryEvents.StockShort;

/**
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
class EventPublicationRegistryIntegrationTests {

	@Test
	void logsFailedEventPublicationsOnClose() {

		new ApplicationContextRunner()
				.withInitializer(new ConfigDataApplicationContextInitializer())
				.withInitializer(context -> {
					context.addApplicationListener(new EnvironmentPostProcessorApplicationListener());
					context.addApplicationListener(new LoggingApplicationListener());
				})
				.withUserConfiguration(AcmeCommerce.class)
				.withBean(SomeTestEventListener.class)
				.withBean(SomeComponent.class)
				.withPropertyValues("logging.level=WARN")
				.run(context -> {

					context.getBean(SomeComponent.class).someMethod();

					Thread.sleep(40);

					var registry = context.getBean(EventPublicationRegistry.class);
					var publications = registry.findIncompletePublications();

					assertThat(publications).hasSize(1);
				});
	}

	@TestComponent
	@RequiredArgsConstructor
	static class SomeComponent {

		private final ApplicationEventPublisher publisher;

		@Transactional
		public void someMethod() {

			var productIdentifier = new ProductIdentifier(UUID.randomUUID());
			var item = new InventoryItem(productIdentifier, Quantity.of(10));

			publisher.publishEvent(StockShort.of(item, Quantity.of(15)));
		}
	}

	@TestComponent
	static class SomeTestEventListener {

		@Async
		@TransactionalEventListener
		void on(StockShort event) {
			throw new IllegalStateException("¯\\_(ツ)_/¯");
		}
	}
}
