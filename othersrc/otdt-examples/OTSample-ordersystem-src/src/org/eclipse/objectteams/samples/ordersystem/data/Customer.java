/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.data;

/**
 * @author Dehla
 *
 */
public class Customer {
	
	@SuppressWarnings("unused") private int id;

	private String lastname;
	private String firstname;
	private Address address;
	
	public Customer(String lastname, String firstname, Address address) {
		this.lastname = lastname;
		this.firstname = firstname;
		this.address = address;
	}
	
	Customer() { }


	/**
	 * @return Returns the address.
	 */
	public Address getAddress() {
		return address;
	}
	
	/**
	 * @param address The address to set.
	 */
	public void setAddress(Address address) {
		this.address = address;
	}
	
	/**
	 * @return Returns the firstname.
	 */
	public String getFirstname() {
		return firstname;
	}
	
	/**
	 * @param firstname The firstname to set.
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	/**
	 * @return Returns the lastname.
	 */
	public String getLastname() {
		return lastname;
	}
	
	/**
	 * @param lastname The lastname to set.
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String toString() {
		return firstname + " " + lastname + "\n" + "private address: " + address.toString();
	}
}
