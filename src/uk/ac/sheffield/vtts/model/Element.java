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
 * Element is the root Element of the metamodel.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Element {
	
	/**
	 * Safe equality comparison that performs the null-checks.  Objects one
	 * and two are equal if they are both null, or if they are both non-null
	 * and one.equals(two) is true.  Otherwise they are not equal.
	 * @param one the first object.
	 * @param two the second object.
	 * @return true if the objects are equal.
	 */
	protected boolean safeEquals(Object one, Object two) {
		return one == null ? two == null : one.equals(two);
	}
	
	/**
	 * Safe hash code function that performs the null-check.  The hash code
	 * for a non-null object is object.hashCode(), otherwise it is zero.
	 * @param object the object to hash.
	 * @return the hash code.
	 */
	protected int safeHashCode(Object object) {
		return object == null ? 0 : object.hashCode();
	}
	
	/**
	 * Reports a semantic error during model construction.
	 * @param message the error message.
	 * @throws SemanticError always.
	 */
	protected void semanticError(String message) throws SemanticError {
		String entity = getClass().getSimpleName() + " '" + this + "' ";
		throw new SemanticError(entity + message, this);
	}

}
