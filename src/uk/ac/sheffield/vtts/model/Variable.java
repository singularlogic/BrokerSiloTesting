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

package uk.ac.sheffield.vtts.model;


/**
 * Variable is a state parameter representing part of the system's memory.
 * A Variable is a modifiable state parameter that is initialised to a value
 * prior to the start of a system's execution.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Variable extends Parameter {

	/**
	 * Creates a default Variable parameter.
	 */
	public Variable() {
	}

	/**
	 * Creates a variable parameter with the given name and type.
	 * @param name the name of this Variable.
	 * @param type the type of this Variable.
	 */
	public Variable(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Takes a snapshot of this Variable.  Clones this Variable, and converts
	 * the bound value of the clone into a String.  Used when creating a TestStep,
	 * to store a snapshot of the Variable value.
	 * @return a clone of this Variable, with a snapshot of its value.
	 */
	@Override
	public Variable snapshot() {
		Variable result = new Variable(name, type);
		if (value != null)
			result.setContent(value.toString());
		return result;
	}

}
