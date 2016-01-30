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
 * Transition represents a named transition in a finite state machine.  A
 * Transition is named using a "request/response" compound naming format,
 * including the dividing forward-slash.  The request and response parts are
 * are given in "camelCase".  A Transition is uniquely indexed by this name 
 * within its owning State, where it is one of several exit Transitions from
 * that State.  A Transition is otherwise uniquely identified by the 
 * combination of source and target State names and its given name.  
 * A Transition is associated with a triggering Event having the same name.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Transition extends Coverable {
	
	/**
	 * The name of the source State.
	 */
	private String source;
	
	/**
	 * The name of the target State.
	 */
	private String target;
	
	/**
	 * Creates a default Transition.
	 */
	public Transition() {
	}
	
	/**
	 * Creates a named Transition.
	 * @param name the name of this Transition.
	 */
	public Transition(String name) {
		super(name);
	}
	
	/**
	 * Reports whether this Transition is equal to another object.  True,
	 * if the other object is a Transition with the same name, source and
	 * target States as this Transition.
	 * @param other the other object.
	 * @return true, if both objects are Transitions with the same name,
	 * source and target.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof Transition) {
			Transition transition = (Transition) other;
			return safeEquals(name, transition.name) && 
					safeEquals(source, transition.source) &&
					safeEquals(target, transition.target);
		}
		else
			return false;
	}

	/**
	 * Returns a quasi-unique hash code for this Transition.  Returns the 
	 * hash code associated with this Transition's name, source and target
	 * State names.
	 * @return the hash code for this Transition.
	 */
	@Override
	public int hashCode() {
		return (super.hashCode() * 31 + safeHashCode(source)) * 31
				+ safeHashCode(target);
	}
	
	/**
	 * Returns the name of a Transition.  It is an error if the name has
	 * not already been set, when this access-method is invoked.
	 * @return the name.
	 */
	@Override
	public String getName() {
		if (name == null)
			semanticError("must be named for indexing purposes.");
		return name;
	}

	/**
	 * Sets the name of the source State.
	 * @param source the name of the source State.
	 * @return this Transition.
	 */
	public Transition setSource(String source) {
		this.source = source;
		return this;
	}
	
	/**
	 * Returns the name of the source State.
	 * @return the name of the source State.
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Sets the name of the target State.
	 * @param target the name of the target State.
	 * @return this Transition.
	 */
	public Transition setTarget(String target) {
		this.target = target;
		return this;
	}
	
	/**
	 * Returns the name of the target State.
	 * @return the name of the target State.
	 */
	public String getTarget() {
		return target;
	}
	
}
