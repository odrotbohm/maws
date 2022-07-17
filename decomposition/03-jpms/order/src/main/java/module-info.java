module com.acme.commerce.jpms.order {

	exports com.acme.commerce.jpms.order;

	// opens com.acme.commerce.jpms.order;

	requires com.acme.commerce.jpms.core;
	requires com.acme.commerce.jpms.catalog;
	requires com.acme.commerce.jpms.inventory;

	requires spring.context;
	requires spring.beans;
	requires spring.core;
}
