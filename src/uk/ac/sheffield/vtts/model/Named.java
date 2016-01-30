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
 * Named is the ancestor of all named elements in the metamodel.  By default,
 * a named element is assumed to be uniquely identifiable by its name.  The
 * contract for equals expects two elements to have the same type and name to
 * be judged equal.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public abstract class Named extends Element {

	/**
	 * The name attribute of a Named element.
	 */
	protected String name;
	
	/**
	 * Creates a default Named element with no name.
	 */
	protected Named() {
	}
	
	/**
	 * Creates a Named element with the given name.  If a second name is 
	 * later assigned, this must be the same name.
	 * @param name the name.
	 */
	protected Named(String name) {
		this.name = name;
	}
	
	/**
	 * Reports whether this Named element is equal to another object.  True,
	 * if the other object is a Named element, whose name is equal to the 
	 * name of this object.
	 * @param other the other object.
	 * @return true, if both Named elements have the same name.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof Named) {
			Named named = (Named) other;
			return safeEquals(name, named.name);
		}
		else
			return false;
	}
	
	/**
	 * Returns a quasi-unique hash code for this named element.  Returns the 
	 * hash code associated with the Named element's name.  This method must 
	 * be overridden in any subclass for which equality is not judged by name
	 * equality.
	 * @return the hash code for this Named element.
	 */
	@Override
	public int hashCode() {
		return safeHashCode(name);
	}
	
	/**
	 * Sets the name of a Named element.  This Named element may only be given
	 * one name and may not be renamed later with a different name.  
	 * @param name the name to set.
	 * @return this Named element.
	 * @throws SemanticError if this Named element is renamed.
	 */
	public Named setName(String name) {
		if (this.name == null)
			this.name = name;
		else if (! name.equals(this.name))
			semanticError("already named '" + this.name + 
					"' is being renamed '" + name + "'.");
		return this;
	}
	
	/**
	 * Sets the name of a Named element to an obligatory name.  This Named 
	 * element must have the given name.  If it already has some other name,
	 * then this breaks a required correspondence in the model.
	 * @param name the name to set.
	 * @param obligatory the value true.
	 * @return this Named element.
	 * @throws SemanticError if this Named element has another name.
	 */
	public Named setName(String name, boolean obligatory) {
		if (this.name == null)
			this.name = name;
		else if (! name.equals(this.name))
			semanticError("should be named '" + name + 
					"' rather than '" + this.name + "'.");
		return this;
	}
	
	/**
	 * Returns the name of a Named element.  Names are optional by default, but
	 * some subtypes insist on the presence of a name, for indexing purposes.
	 * @return the name, or null.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Converts this Named element to a printable representation.  By default,
	 * returns the name of this Named element.
	 * @return the String representation of this Named element.
	 */
	@Override
	public String toString() {
		return name;
	}

}
