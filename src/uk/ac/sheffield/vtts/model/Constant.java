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

import java.util.Collections;



/**
 * Constant is a kind of parameter standing for some fixed constant value.
 * Constant represents a symbolic constant that is bound to a literal value.
 * The Constant is typically named according to the bound value, such that 
 * "zero" represents the value 0, "true" represents the value true and
 * "emptyList" represents an empty list.  The bound value is supplied as an
 * XML content string and is converted to a strongly-typed bound value when 
 * the Constant is first evaluated.  Empty content is always interpreted as
 * a default bound value, according to the type of the Constant.  A Constant
 * is therefore always bound.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Constant extends Parameter {

	/**
	 * Creates a default Constant parameter.
	 */
	public Constant() {
		rebind();  // Set bound flag to true
	}

	/**
	 * Creates a Constant parameter with the given name and type.
	 * @param name the name of this Constant.
	 * @param type the type of this Constant.
	 */
	public Constant(String name, String type) {
		super(name, type);
		rebind();  // Set bound flag to true
	}
	
	/**
	 * Degenerate method to set the bound status of this Constant.
	 * @param value is ignored, since Constants are always bound.
	 * @return this Constant.
	 */
	public Constant setBound(boolean value) {
		return this;
	}
	
	/**
	 * Degenerate method to fetch the bound status for marshalling.
	 * Constants do not need to marshal their bound status, since they are
	 * always bound.
	 * @return null.
	 */
	public Boolean getBound() {
		return null;
	}
	
	/**
	 * Degenerate method to unbind this Constant.  This is a null operation,
	 * since Constants are never unbound, but always keep their bound value.
	 * Likewise, the stored content String for a Constant is never deleted.
	 */
	@Override
	public void unbind() {
	}
	
	/**
	 * Degenerate method to rebind this Constant to a new value.  This is a
	 * null operation, since constants cannot be rebound to a different value
	 * other than the one they had at creation.
	 */
	@Override
	public void rebind(Object value) {
	}
	
	/**
	 * Reports whether this Constant is the bottom element of its type.
	 * This is true for the null Character, the empty String, Set, List
	 * or Map; and false for all other values.
	 * This judgement is used when refining the partitions of a comparison,
	 * to decide whether lessThan(x, y) is a meaningful partition.
	 * @return true, if this Constant is the bottom element of its type.
	 */
	@Override
	public boolean isBottom() {
		Object constValue = evaluate();
		return (constValue == null || constValue.equals('\0') || constValue.equals("") || 
				constValue.equals(Collections.emptySet()) || 
				constValue.equals(Collections.emptyList()) ||
				constValue.equals(Collections.emptyMap()));
	}
	
	/**
	 * Reports whether this Constant is assignable.  Returns false, since it
	 * is illegal to re-assign a new value to a Constant.
	 * @return false, always.
	 */
	@Override
	public boolean isAssignable() {
		return false;
	}
	
	/**
	 * Reports an attempt to assign to this Constant as an error.
	 */
	@Override
	public void assign(Object value) {
		semanticError("cannot be re-assigned a new value.");
	}
	
}
