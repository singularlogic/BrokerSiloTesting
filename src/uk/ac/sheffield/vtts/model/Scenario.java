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
import java.util.Set;

/**
 * Scenario represents one branching path through a single operation.  An
 * Operation consists of one or more Scenarios, each of which takes a distinct
 * logical path.  
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Scenario extends Event {
	
	/**
	 * The binding of input values.
	 */
	private Binding binding;
	
	/**
	 * The guard condition.
	 */
	private Condition condition;
	
	/**
	 * The update effect.
	 */
	private Effect effect;
	
	/**
	 * Creates a default Scenario.
	 */
	public Scenario() {
	}
	
	/**
	 * Creates a named Scenario.
	 * @param name the name of this Scenario.
	 */
	public Scenario(String name) {
		super(name);
	}
	
	/**
	 * Adds a Binding of initial Expressions to Variables to this Scenario.
	 * @param binding the Binding to add.
	 * @return this Memory.
	 */
	public Scenario addBinding(Binding binding) {
		this.binding = binding;
		return this;
	}
	
	/**
	 * Returns the Binding of Expressions to Variables in this Scenario.
	 * @return the initial Binding.
	 */
	public Binding getBinding() {
		return binding;
	}
	
	/**
	 * Adds the guard Condition for this Scenario.
	 * @param condition the condition to add.
	 * @return this response.
	 */
	public Scenario addCondition(Condition condition) {
		this.condition = condition;
		return this;
	}

	/**
	 * Returns the guard Condition for this Scenario.
	 * @return the guard Condition.
	 */
	public Condition getCondition() {
		return condition;
	}
	
	/**
	 * Returns the set of all possible atomic predicates and their negations
	 * that can be derived from this Scenario's Condition.  The returned set
	 * is not structured in any way.  The structuring of partitions is 
	 * performed by Operation.  Returns an empty set if this Scenario has
	 * no Condition.
	 * @return a set of Predicates.
	 */
	public Set<Predicate> getPartitions() {
		if (condition == null)
			return Collections.emptySet();
		else
			return condition.getPartitions();
	}

	/**
	 * Adds the Effect,the updated binding of Expressions to Parameters.
	 * @param effect the side-effect.
	 * @return this Response.
	 */
	public Scenario addEffect(Effect effect) {
		this.effect = effect;
		return this;
	}
	
	/**
	 * Returns the Effect, the updated binding of Expressions to Parameters.
	 * @return the side-effect.
	 */
	public Binding getEffect() {
		return effect;
	}
	
	/**
	 * Causes this Scenario to resolve its global/local Parameter references.
	 * The Scope is a table of Parameters constructed from global Memory and 
	 * the owning local Operation.  Delegates to the initial Binding, guard
	 * Condition and final Effect, where present, causing these to resolve 
	 * their global/local Parameter references.
	 * @param scope a Scope containing global and local Parameters.
	 * @return this Scenario.
	 */
	public Scenario resolve(Scope scope) {
		if (binding != null)
			binding.resolve(scope);
		if (condition != null) 
			condition.resolve(scope);
		if (effect != null)
			effect.resolve(scope);
		return this;
	}
	
	/**
	 * Reports whether this Scenario is enabled in the current Memory state.
	 * If a Binding is provided, binds any Inputs.  If a Condition is 
	 * provided, tests the guard Condition, which may refer to Inputs and 
	 * Variables from the Memory.  If the guard Condition is satisfied,
	 * returns true.
	 * @return true, if this Scenario is enabled.
	 */
	public boolean isEnabled() {
		if (binding != null)
			binding.execute();
		if (condition != null) {
			return condition.evaluate();
		}
		else
			return true;  // by default
	}
	
	/**
	 * Conditionally executes this Scenario.  If a Binding is provided, binds
	 * any Inputs.  If a Condition is provided, tests the guard Condition,
	 * which may refer to Inputs and Variables from the Memory.  If the guard
	 * Condition is satisfied and an Effect is provided, executes the Effect. 
	 * @return true, if the Condition was satisfied, otherwise false.
	 */
	public boolean execute() {
		if (isEnabled()) {
			//DEBUG
			//System.out.println("Executing scenario: " + name);
			if (effect != null)  // Could be a Scenario with no effect
				effect.execute();
			return true;
		}
		else
			return false;
	}

}
