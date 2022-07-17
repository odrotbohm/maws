package com.acme.commerce.jpms.order;

import org.springframework.stereotype.Component;

import com.acme.commerce.jpms.inventory.Inventory;

@Component
public class OrderManagement {

	OrderManagement(Inventory inventory) {

	}

	void someMethod() {
		System.out.println("Foo!");
	}
}
