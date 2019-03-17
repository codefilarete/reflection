package org.gama.reflection.model;

/**
 * @author Guillaume Mary
 */
public class Person {
	
	private Address address;
	
	private String name;
	
	private String lastName;
	
	public Person(Address address) {
		this.address = address;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
