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
package com.acme.commerce.order;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.util.Streamable;

import com.acme.commerce.customer.Customer.CustomerIdentifier;
import com.acme.commerce.order.Order.OrderIdentifier;

/**
 * Repository interface for {@link Order}s.
 *
 * @author Oliver Drotbohm
 */
interface OrderRepository
		extends CrudRepository<Order, OrderIdentifier>, PagingAndSortingRepository<Order, OrderIdentifier> {

	/**
	 * Re-declaration of the method actually already contained in {@link PagingAndSortingRepository} to use the JPQL based
	 * variant of {@link Sort} binding, as only that allows the definition of expressions referencing properties of
	 * sub-types of {@link Order}, too.
	 *
	 * @param pageable must not be {@literal null}.
	 * @return
	 */
	@Override
	@Query("select o from #{#entityName} o")
	Page<Order> findAll(Pageable pageable);

	/**
	 * @param from
	 * @param to
	 * @return
	 */
	Streamable<Order> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	/**
	 * @param orderStatus
	 * @return
	 */
	Streamable<Order> findByOrderStatus(OrderStatus orderStatus);

	/**
	 * @param userAccount
	 * @return
	 */
	Streamable<Order> findByCustomer(CustomerIdentifier userAccount);

	/**
	 * @param userAccount
	 * @param from
	 * @param to
	 * @return
	 */
	Streamable<Order> findByCustomerAndDateCreatedBetween(CustomerIdentifier userAccount, LocalDateTime from,
			LocalDateTime to);
}
