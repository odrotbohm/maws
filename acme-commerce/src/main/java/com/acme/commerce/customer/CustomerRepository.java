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
package com.acme.commerce.customer;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import com.acme.commerce.customer.Customer.CustomerIdentifier;

/**
 * A repository interface to manage {@link Customer} instances.
 *
 * @author Oliver Drotbohm
 */
public interface CustomerRepository
		extends CrudRepository<Customer, CustomerIdentifier>, AssociationResolver<Customer, CustomerIdentifier> {

	/**
	 * Re-declared {@link CrudRepository#findAll()} to return a {@link Streamable} instead of {@link Iterable}.
	 */
	@Override
	Streamable<Customer> findAll();
}
