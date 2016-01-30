/**
 * Broker@Cloud Verification and Testing Tool Suite.
 * Copyright (C) 2015 Anthony J H Simons and Raluca Lefticaru, 
 * University of Sheffield, UK.  All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This file is "foreground software", developed as an output of 
 * the European Union collaborative research project, "Broker@Cloud: 
 * enabling continuous quality assurance and optimization in future 
 * enterprise cloud service brokers", FP7-ICT-2011-8 no. 318392, and
 * is licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.  See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * The freedoms granted by the License to incorporate, redistribute,
 * modify or extend the software apply only to "foreground software"
 * contributed by the Broker@Cloud project; and not to any proprietary 
 * software, or "background software" incorporated from other sources, 
 * which may be offered under different terms of usage.
 * 
 * Please contact the Department of Computer Science, University of
 * Sheffield, Regent Court, 211 Portobello, Sheffield S1 4DP, UK or
 * visit www.sheffield.ac.uk/dcs if you need additional information 
 * or have any questions.
 */

package uk.ac.sheffield.vtts.client.ws;

/**
 * ShoppingCartInterface is a skeleton of the JAX-WS client interface for
 * the JAX-WS implementation of the <code>ShoppingCart.xml</code> example.
 * This is a placeholder until you develop your own interface using JAX-WS
 * tools.
 *
 * @author Anthony J H Simons
 * @version 1.0
 */
public interface ShoppingCartInterface {

	/**
	 * Resets the service to its clean initial state.
	 */
	void reset();

	/**
	 * Returns the last scenario that was enacted.
	 * @return the last scenario that was enacted.
	 */
	String getScenario();

	/**
	 * Returns the last state that was entered.
	 * @return the last state that was entered.
	 */
	String getState();

	/**
	 * Enters the shop, to start adding items to the cart.
	 */
	void enterShop();

	/**
	 * Exits the shop, discarding all items chosen so far.
	 */
	void exitShop();

	/**
	 * Adds a DVD to the shopping cart.
	 * @param dvd the selected DVD to add.
	 * @return the new quantity of this item in the cart.
	 */
	Integer addItem(Dvd dvd);

	/**
	 * Removes a DVD from the shopping cart.
	 * @param dvd the selected DVD to remove.
	 * @return the new quantity of this item in the cart.
	 */
	Integer removeItem(Dvd dvd);

	/**
	 * Removes all purchases from the shopping cart.
	 */
	void clearItems();

	/**
	 * Advances to the checkout, if the cart contains some items.
	 * @return true, if items exist; otherwise false.
	 */
	Boolean checkout();

	/**
	 * Returns the total amount owing for all purchases.
	 * @return the bill.
	 */
	Float getBill();

	/**
	 * Supply billing and shipping information at the checkout.  If the
	 * billing information is valid, advances to payment.
	 * @param billingInfo the billing information.
	 * @param addressInfo the shipping address.
	 * @return true, if the billing information was valid.
	 */
	Boolean payBill(Integer billingInfo, String addressInfo);

	/**
	 * Confirms the amount to pay, completes the transaction and returns
	 * to the ready state.
	 * @return the total amount paid.
	 */
	Float confirm();

}
