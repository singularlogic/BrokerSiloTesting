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

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Memory represents the collection of state variables in a software service.
 * The memory represents significant state values that affect the behaviour
 * of a service.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Memory extends Scope {
	
	/**
	 * The initial binding of Constant values to Variable parameters.
	 */
	private Binding binding;
	
	/**
	 * Creates a default Memory.
	 */
	public Memory() {
		super();
	}
	
	/**
	 * Creates a Memory with a name.  A Memory may optionally be named, where
	 * any supplied name must then match the owning Service's name.
	 * @param name the name of this Memory.
	 */
	public Memory(String name) {
		super(name);
	}
		
	/**
	 * Returns the set of Constants in this Memory.  A convenience, to extract
	 * those Parameters which are Constants.
	 * @return the Constants in this Memory.
	 */
	public Set<Constant> getConstants() {
		Set<Constant> result = new LinkedHashSet<Constant>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Constant)
				result.add((Constant) parameter);
		}
		return result;
	}

	/**
	 * Returns the set of Variables in this Memory.  A convenience, to extract
	 * those Parameters which are Variables.
	 * @return the Variables in this Memory.
	 */
	public Set<Variable> getVariables() {
		Set<Variable> result = new LinkedHashSet<Variable>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Variable)
				result.add((Variable) parameter);
		}
		return result;
	}

	/**
	 * Adds a Binding of initial Constants to Variables to this Memory scope.
	 * @param binding the Binding to add.
	 * @return this Memory.
	 */
	public Memory addBinding(Binding binding) {
		this.binding = binding;
		return this;
	}
	
	/**
	 * Returns the Binding of Memory Variables to their initial values.  May 
	 * be null if the Memory only contains Constants.
	 * @return the initial Binding.
	 */
	public Binding getBinding() {
		return binding;
	}
	
	/**
	 * Cause this Memory to resolve its global Parameter references.
	 * Triggered when this Memory is added to its owning Service.  The Scope
	 * argument is the Memory itself, which contains the global Constants and
	 * Variables.  Delegates to the initial Binding, causing this to resolve
	 * all references to global Parameters in the Scope.
	 * @param scope the Scope containing global Parameters.
	 * @return this Memory.
	 */
	public Memory resolve(Scope scope) {
		if (binding != null)
			binding.resolve(scope);
		return this;
	}
	
	/**
	 * Rebinds this Memory.  Sets the Variables in this Memory to initial
	 * values, according to the initial Assignments expressed.  May be called
	 * to re-initialise this Memory after each simulation of the Service.
	 * @return this Memory.
	 */
	public Memory rebind() {
		if (binding != null)
			binding.execute();
		return this;
	}
	
	/**
	 * Checks the completeness of this Memory.  Checks that any Variables
	 * declared in this Memory are properly initialised in the Binding.  
	 * Attaches any generated Warning to the supplied Analysis node.
	 * @param topInfo the root Analysis node.
	 * @return this Memory.
	 */
	public Memory checkCompleteness(Notice topInfo) {
		String name = getName() == null ? "unnamed" : getName();
		Notice memInfo = new Notice(
				"Initialisation check for memory: " + name);
		addNotice(memInfo);
		Set<Variable> variables = getVariables();
		if (variables.isEmpty()) {
			memInfo.addNotice(new Analysis(
					"Memory requires no initialisation"));
			topInfo.addNotice(new Analysis(
					"Memory requires no initialisation"));
		}
		else {  // subtract any initialised variables
			if (binding != null) {
				for (Assignment assign : binding.getAssignments()) {
					Variable variable = (Variable) assign.operand(0);
					variables.remove(variable);
				}
			}
			if (variables.isEmpty()) {
				memInfo.addNotice(new Analysis(
						"Memory is correctly initialised"));
				topInfo.addNotice(new Analysis(
						"Memory is correctly initialised"));
			}
			else {
				for (Variable variable : variables) {
					memInfo.addNotice(new Warning(
							"Variable is never initialised: " + variable));
				}
				topInfo.addNotice(new Warning(
						"Memory is not correctly initialised"));
			}
		}
		return this;
	}
	
}
