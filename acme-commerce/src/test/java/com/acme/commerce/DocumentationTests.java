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
package com.acme.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.CanvasOptions;
import org.springframework.modulith.docs.Documenter.Options;
import org.springframework.modulith.model.ApplicationModules;

/**
 * @author Oliver Drotbohm
 */
class DocumentationTests {

	@Test
	void generateDocumentation() throws Exception {

		var modules = ApplicationModules.of(AcmeCommerce.class);
		modules.verify();

		modules.forEach(System.out::println);

		var options = Options.defaults();
		var canvasOptions = CanvasOptions.defaults();

		new Documenter(modules).writeDocumentation(options, canvasOptions);
	}
}
