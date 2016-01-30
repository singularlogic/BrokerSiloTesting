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
 * Parameter is a kind of expression denoting a named and typed parameter.  
 * Parameter is the ancestor of Constant, Variable, Input and Output, which
 * are the actual kinds of Parameter used in model specifications.  A 
 * Parameter may be supplied with an XML content string, which is converted 
 * to a strongly-typed object when the Parameter is first evaluated.  With 
 * the exception of Constant, all other kinds of Parameter may either be 
 * bound or unbound.  When a Parameter is unbound, its value is null and
 * evaluating will return null.  When a Parameter is bound, evaluating will
 * always return a non-null strongly-typed object.  This will either be the
 * cached value, or a value created from the XML content string, possibly
 * using default initialisation rules if no content exists.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public abstract class Parameter extends Expression {
	
	/**
	 * Flag denoting whether this Parameter is currently bound.  Initially
	 * false for all Parameters, except Constants, which are always bound,
	 * this may be set to true by Assignment of some value, and may also be 
	 * affected by calls to rebind, or unbind.
	 */
	protected boolean bound;
	
	/**
	 * The strongly-typed bound value of this Parameter, or null.
	 */
	protected Object value;
	
	/**
	 * The textual printed representation of the value, or null.
	 */
	protected String content;
	
	/**
	 * Creates a default parameter.
	 */
	public Parameter() {
	}
		
	/**
	 * Creates a parameter of the given name and type.
	 * @param name the name of this parameter.
	 * @param type the type of this parameter.
	 */
	public Parameter(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Reports whether this Parameter is re-assignable.  By default, the
	 * result is true, since most Parameters may be re-assigned new values
	 * in an Assignment expression.  This method is overridden in Constant,
	 * to return false, since a Constant may not be re-assigned, but may
	 * only be initialised.
	 * @return true, by default.
	 */
	public boolean isAssignable() {
		return true;
	}
	
	/**
	 * Evaluates this Parameter expression.  Returns the bound value of this
	 * Parameter, if it is currently bound.  If a bound value exists, returns
	 * this; otherwise creates the bound value from the specified textual 
	 * content and caches it.  If no content is specified, performs default
	 * initialisation to false, zero, null byte, empty String, empty List,
	 * Set or Map, etc. according to type.  If this Parameter is not bound,
	 * returns null.
	 * @return the bound value of this Parameter.
	 */
	@Override
	public Object evaluate() {
		if (bound && value == null)
			value = factory.createObject(content, type);
		return value;
	}
	
	/**
	 * Sets whether this Parameter is bound.  Initially this Parameter is not
	 * bound, unless it is a Constant.
	 * @param value true, if this Parameter is bound.
	 * @return this Parameter.
	 */
	public Parameter setBound(boolean value) {
		bound = value;
		return this;
	}
	
	/**
	 * Optionally returns the value of the bound flag for marshalling.  This
	 * returns true only if this Parameter is assignable and also bound to a
	 * value; otherwise returns null.  Redefined in Constant to return null,
	 * to reduce XML clutter, since Constants are always bound.
	 * @return the Boolean object true, or null.
	 */
	public Boolean getBound() {
		return bound ? true : null;
	}
	
	/**
	 * Reports whether this Parameter is bound.  Returns the value of the
	 * bound flag.  Use this in preference to getBound() when testing the
	 * bound status, since this is the proper query method.
	 * @return true if this Parameter is bound.
	 */
	public boolean isBound() {
		return bound;
	}
	
	/**
	 * Unbinds this Parameter.  Sets the bound flag to false, and deletes 
	 * any cached copy of this Parameter's strongly-typed bound value.  This
	 * is invoked during protocol checking, when searching for values that
	 * may satisfy a compound condition.
	 */
	public void unbind() {
		bound = false;
		value = null;
	}
	
	/**
	 * Rebinds this Parameter to its default value.  Sets the bound flag to
	 * true, so that any subsequent evaluation will create and cache a 
	 * default value, according to type.  This is invoked during protocol
	 * checking, when searching for values that may satisfy a compound
	 * condition.
	 */
	public void rebind() {
		bound = true;
	}
	
	/**
	 * Rebinds this Parameter to the given value.  Sets the bound flag to
	 * true.  Assumes that the value is in fact of the correct type.
	 * @param value the value to bind.
	 */
	public void rebind(Object value) {
		bound = true;
		this.value = value;
	}
	
	/**
	 * Degenerate method to resolve Parameter references in this Parameter.
	 * This is a null operation, since a Parameter is an atomic expression 
	 * and contains no further embedded Parameter references that need to
	 * be resolved.
	 * @param scope a table of global and local Parameters currently in 
	 * scope.
	 */
	public Parameter resolve(Scope scope) {
		return this;
	}
	
	/**
	 * Assigns a new bound value to this Parameter.  The value is the result
	 * of evaluating some other Expression, which is now being bound to this
	 * Parameter.  The value is cached and the bound flag is set to true.
	 * This method is overridden in Constant, which may not reassign values.
	 * @param value the value to assign.
	 */
	@Override
	public void assign(Object value) {
		bound = true;
		this.value = value;
	}

	/**
	 * The printable representation of a parameter is its name.
	 * @return the name of this parameter.
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Sets the content of this Parameter.
	 * @param content the content, as a String.
	 * @return this Parameter.
	 */
	public Parameter setContent(String content) {
		this.content = content;
		return this;
	}

	/**
	 * Returns the content of this Parameter.
	 * @return the content of this Parameter.
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Takes a snapshot of this Parameter, fixing its current bound value.
	 * Clones this Parameter, whether it is a Constant, Variable, Input or
	 * output, creating a true copy.  Converts its current bound value back
	 * to a String value, so that the snapshot is ready to be marshalled as
	 * XML data.  This is used during test generation, to create snapshots
	 * of Parameters that are shared by other test cases, before they are
	 * rebound in the next test case.
	 * @return a clone of this Parameter, with a snapshot of its value.
	 */
	public Parameter snapshot() {
		Parameter result = null;
		try {
			result = getClass().newInstance();
			result.setName(name);
			result.setType(type);
			result.setBound(bound);
			if (bound && value != null)
				result.setContent(value.toString());
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

}
