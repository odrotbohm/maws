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
package com.acme.commerce.catalog;

import java.util.Arrays;
import java.util.Collection;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.util.Streamable;

import com.acme.commerce.catalog.Product.ProductIdentifier;

/**
 * Repository interface for {@link Product}s
 *
 * @author Oliver Drotbohm
 */
public interface Catalog
		extends CrudRepository<Product, ProductIdentifier>, PagingAndSortingRepository<Product, ProductIdentifier>,
		AssociationResolver<Product, ProductIdentifier> {

	/**
	 * Returns all {@link Product}s assigned to the given category.
	 *
	 * @param category
	 * @return
	 */
	@Query("select p from #{#entityName} p where :category member of p.categories")
	Streamable<Product> findByCategory(String category);

	/**
	 * Returns all {@link Product} that are assigned to all given categories.
	 *
	 * @param categories must not be {@literal null}.
	 * @return
	 */
	default Streamable<Product> findByAllCategories(String... categories) {
		return findByAllCategories(Arrays.asList(categories));
	}

	/**
	 * Returns all {@link Product} that are assigned to all given categories.
	 *
	 * @param categories must not be {@literal null}.
	 * @return
	 */
	@Query("select  p from #{#entityName} p " + //
			"where (select count(c) " + //
			"from #{#entityName} p2 inner join p2.categories c " + //
			"where p2.id = p.id and c in :categories) = ?#{#categories.size().longValue()}")
	Streamable<Product> findByAllCategories(Collection<String> categories);

	/**
	 * Returns all {@link Product}s that are assigned to any of the given categories.
	 *
	 * @param categories must not be {@literal null}.
	 * @return
	 */
	default Streamable<Product> findByAnyCategory(String... categories) {
		return findByAnyCategory(Arrays.asList(categories));
	}

	/**
	 * Returns all {@link Product}s that are assigned to any of the given categories.
	 *
	 * @param categories must not be {@literal null}.
	 * @return
	 */
	@Query("select p from #{#entityName} p join p.categories c where c in :categories")
	Streamable<Product> findByAnyCategory(Collection<String> categories);

	/**
	 * Returns the {@link Product}s with the given name.
	 *
	 * @param name
	 * @return
	 */
	Streamable<Product> findByName(String name);
}
