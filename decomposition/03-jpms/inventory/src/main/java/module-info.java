module com.acme.commerce.jpms.inventory {

	exports com.acme.commerce.jpms.inventory;

	opens com.acme.commerce.jpms.inventory;

	requires spring.context;
	requires spring.beans;
	requires spring.core;
}
