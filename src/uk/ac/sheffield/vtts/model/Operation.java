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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Operation represents an operation in the public interface of a service.
 * An Operation represents a local Scope in which Input and Output variables
 * may be declared.  When included as part of a Protocol specification, an
 * Operation maintains a collection of Scenarios, each representing a
 * distinct path through the Operation.  When an Operation is included in
 * a TestStep, the Scenarios are not copied, but any Input and Output
 * parameters will be bound to specific values.  An Operation is a kind of 
 * Annotated element that may receive Warnings.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Operation extends Scope {
	
	/**
	 * The Scenarios constituting the different paths through this Operation.
	 * This field may remain null until the first Scenario is added.  Some
	 * uses of Operation do not require the Scenarios.
	 */
	private Map<String, Scenario> scenarios = null;
		
	/**
	 * Creates a default Operation.
	 */
	public Operation() {
	}
	
	/**
	 * Creates a named Operation.
	 * @param name the name of this Operation.
	 */
	public Operation(String name) {
		super(name);
	}
	
	/**
	 * Reports whether this Operation is the initial service creation step.
	 * This test is used during grounding, to support bespoke code generation
	 * to create or initialise the service.
	 * @return true, if the name of this Operation is "create".
	 */
	public boolean isCreation() {
		return getName().equals("create");
	}
	
	/**
	 * Reports whether this Operation represents an exceptional failure step.
	 * This test is used during grounding, to support bespoke code generation
	 * to handle modelled service exceptions.
	 * @return true, if this Operation's result is bound to a Failure.
	 */
	public boolean isFailure() {
		for (Failure failure : getFailures()) {
			if (failure.isBound())
				return true;
		}
		return false;
	}

	/**
	 * Returns the set of Inputs in this Operation.  A convenience, to extract
	 * those Parameters from the Scope which are Inputs.
	 * @return the Inputs in this Operation.
	 */
	public Set<Input> getInputs() {
		Set<Input> result = new LinkedHashSet<Input>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Input)
				result.add((Input) parameter);
		}
		return result;
	}
	
	/**
	 * Returns the set of Outputs in this Operation.  A convenience, to extract
	 * those Parameters from the Scope which are Outputs.
	 * @return the Outputs in this Operation.
	 */
	public Set<Output> getOutputs() {
		Set<Output> result = new LinkedHashSet<Output>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Output)
				result.add((Output) parameter);
		}
		return result;
	}
	
	/**
	 * Returns the set of Failures of this Operation.  A convenience, to extract
	 * those Parameters from the Scope which are Failures.
	 * @return the Failures of this Operation.
	 */
	public Set<Failure> getFailures() {
		Set<Failure> result = new LinkedHashSet<Failure>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Failure)
				result.add((Failure) parameter);
		}
		return result;
	}
	
	/**
	 * Cause this Operation to resolve its global/local Parameter references.
	 * Triggered when the Protocol owning this Operation is added to its 
	 * owning Service.  The Scope argument is the global Memory, which
	 * contains the global Constants and Variables.  Constructs a new Scope,
	 * which contains all the global Parameters in the argument, to which are
	 * added all the local Parameters contained in this Operation.  Delegates
	 * to each Scenario in turn, passing it the complete table of Parameters.
	 * @param scope a Scope containing global Parameters.
	 * @return this Operation.
	 */
	public Operation resolve(Scope scope) {
		for (Scenario scenario : getScenarios()) {
			Scope table = new Scope();
			table.addScope(scope);
			table.addScope(this);
			scenario.resolve(table);
		}
		return this;
	}
	
	/**
	 * Adds a Scenario to this Operation.  Each Scenario must be uniquely
	 * named using the format: "request/response".  The "request" part must
	 * be equal to the name of this Operation.  The table of Scenarios is
	 * only created when the first Scenario is added.
	 * @param scenario the Scenario to add.
	 * @return this Operation.
	 */
	public Operation addScenario(Scenario scenario) {
		if (scenarios == null)
			scenarios = new LinkedHashMap<String, Scenario>();
		scenarios.put(scenario.getName(), scenario);
		return this;
	}
	
	/**
	 * Returns the Scenario with the specified unique name, if it exists.
	 * @param name the name of the Scenario.
	 * @return the associated Scenario, or null if no such Scenario exists.
	 */
	public Scenario getScenario(String name) {
		if (scenarios == null)
			return null;
		else
			return scenarios.get(name);
	}
	
	/**
	 * Returns this Operation's Scenarios as an ordered Set.  If Scenarios
	 * were added, returns these; otherwise returns an empty set, rather than
	 * null.
	 * @return an ordered Set of this Operation's Scenarios.
	 */
	public Set<Scenario> getScenarios() {
		if (scenarios == null)
			return Collections.emptySet();
		else
			return new LinkedHashSet<Scenario>(scenarios.values());
	}
	
	/**
	 * Returns the Scenario corresponding to the supplied Event.  Selects the
	 * Scenario from this Operation, whose name is equal to the Event's name.
	 * Raises a semantic error if no such Scenario exists.
	 * @param event the Event.
	 * @return the corresponding Scenario, which must exist.
	 */
	public Scenario getScenario(Event event) {
		Scenario scenario = getScenario(event.getName());
		if (scenario == null) 
			semanticError("has no Scenario named '" + event.getName() + "'.");
		return scenario;
	}
	
	/**
	 * Reports whether this Operation accepts an Event in the current Memory
	 * state.  Selects the Scenario from the Operation, whose name is equal to
	 * the Event's whole name.  Reports whether this Scenario may be executed
	 * in the current Memory environment.  
	 * @param event the Event.
	 * @return true, if the corresponding Scenario can be executed.
	 */
	public boolean accept(Event event) {
		Scenario scenario = getScenario(event.getName());
		if (scenario == null) 
			semanticError("has no Scenario named '" + event.getName() + "'.");
		// DEBUG
		// System.out.println("Operation = " + this + "; testing event: " + event);
		return scenario.isEnabled();
	}
	
	/**
	 * Fires an event on this Operation.  Selects the Scenario from this
	 * Operation, whose name is equal to the Event's name.  Executes the 
	 * Scenario and returns true, if the Scenario was able to execute with
	 * the current Memory state.
	 * @param event an Event.
	 * @return true, if this Operation was able to fire.
	 */
	public boolean fireEvent(Event event) {
		// Outputs previously unbound by TestStep.recordOutputs(outputs)
		// Failures previously unbound by TestStep.recordFailures(failures)
		Scenario scenario = getScenario(event.getName());
		if (scenario == null) 
			semanticError("has no Scenario named '" + event.getName() + "'.");
		// DEBUG
		// System.out.println("Operation = " + this + "; firing event: " + event);
		return scenario.execute();
	}
	
	/**
	 * Checks this Operation for completeness under all memory and input
	 * conditions.  Seeks to determine whether there is a non-blocking and
	 * deterministic response to each partition in the symbolic input space
	 * of the Operation.
	 * @param topInfo the Notice to which global warnings are attached.
	 * @return this Operation.
	 */
	public Operation checkCompleteness(Notice topInfo) {
		Notice opInfo = new Notice(
				"Completeness check for operation: " + getName());
		//DEBUG
		//System.out.println("Checking operation: " + getName());
		addNotice(opInfo);
		// Check that input bindings are present
		checkBindings(topInfo, opInfo);
		// Compute input partitions and evaluate each scenario.
		boolean blocking = false;
		boolean nondeterministic = false;
		Set<Predicate> partitions = getPartitions(opInfo);
		if (partitions.isEmpty()) {
			// No guard conditions, check for one universal scenario.
			List<Analysis> list = new ArrayList<Analysis>();
			for (Scenario scenario : getScenarios()) {
				list.add(new Analysis("Scenario " +
						scenario.getName() + " accepts universal input"));
			}
			if (list.isEmpty()) {
				opInfo.addNotice( new Warning(
						"No scenarios enabled for universal input"));
				blocking = true;				
			}
			else if (list.size() == 1) {
				opInfo.addNotice(list.get(0));
			}
			else {
				Warning warning = new Warning(
						"Multiple scenarios triggered by universal input");
				opInfo.addNotice(warning);
				for (Analysis analysis : list) {
					warning.addNotice(analysis);
				}
				nondeterministic = true;
			}
		}
		else {
			// Some guard conditions, therefore check for each partition.
			int count = 0;
			for (Predicate input : partitions) {
				++count;
				List<Analysis> list = new ArrayList<Analysis>();
				for (Scenario scenario : getScenarios()) {
					Condition condition = scenario.getCondition();
					//DEBUG
					//Predicate guard = condition.getPredicate();
					//System.out.println("Scenario = " +scenario.getName());
					//System.out.println("   formal: " + guard);
					//System.out.println("   actual: " + input);
					if (condition == null || condition.accepts(input)) {
						//DEBUG
						//System.out.println("      accepted!");
						list.add(new Analysis("Scenario " +
								scenario.getName() + " accepts input " + count));
					}
					//DEBUG
					//else System.out.println("      refused!");
				}
				if (list.isEmpty()) {
					opInfo.addNotice( new Warning(
							"No scenarios enabled for input " + count));
					blocking = true;
				}
				else if (list.size() == 1) {
					opInfo.addNotice(list.get(0));
				}
				else {
					Warning warning = new Warning(
							"Multiple scenarios triggered by input " + count);
					opInfo.addNotice(warning);
					for (Analysis analysis : list)
						warning.addNotice(analysis);
					nondeterministic = true;
				}
			}
		}
		if (nondeterministic || blocking) {
			if (nondeterministic) {
				topInfo.addNotice(new Warning(
						"Operation is nondeterministic: " + this));			
			}
			if (blocking) {
				topInfo.addNotice(new Warning(
						"Operation is blocking: " + this));			
			}
		}
		else
			topInfo.addNotice(new Analysis(
					"Operation is deterministic: " + this));
		return this;
	}
	
	/**
	 * Returns the possible symbolic input partitions for this Operation.
	 * For each Scenario, extracts the atomic predicates over each pair of
	 * parameters, discovered by refining the Scenario's Condition and its
	 * complement.  Partitions these predicates into sets, by indexing each
	 * set according to the pair of parameters governed by each predicate.
	 * Converts the partitioned sets into all possible combinations, taking
	 * one predicate from each set.  Converts each combination into a logical
	 * -AND Proposition governing the Predicates in the combination, and 
	 * adds this to the resulting set of Predicates.  If an Operation has a
	 * single Scenario with no Condition, returns an empty set, indicating
	 * that the Operation accepts universal input.  Otherwise, returns a set
	 * of predicates, each representing a different partition of the input
	 * space.
	 * @param opInfo the Notice reporting on this Operation.
	 * @return the partitions of this Operation's input space.
	 */
	protected Set<Predicate> getPartitions(Notice opInfo) {
		// First stage: calculate all atomic predicates over each pair of 
		// Parameters and index these in sets, according to the unique pair
		// of Parameters that each predicate governs.
		Map<Object, Set<Predicate>> indexedSets = 
				new LinkedHashMap<Object, Set<Predicate>>();
		for (Scenario scenario : getScenarios()) {
			for (Predicate predicate : scenario.getPartitions()) {
				Object key = predicate.getExpressions();
				if (!indexedSets.containsKey(key)) {
					indexedSets.put(key,  new LinkedHashSet<Predicate>());
				}
				indexedSets.get(key).add(predicate);
			}
		}
		// Second stage: grow the tree of atomic predicate combinations,
		// combining every permutation of predicates, indexed by the same set
		// of operands, with every other such permutation.
		List<List<Predicate>> oldMatrix = new ArrayList<List<Predicate>>();
		oldMatrix.add(new ArrayList<Predicate>());  // oldMatrix contains empty list
		for (Set<Predicate> partitions : indexedSets.values()) {
			List<List<Predicate>> newMatrix = new ArrayList<List<Predicate>>();
			for (List<Predicate> oldList : oldMatrix) {
				for (Predicate predicate : partitions) {
					List<Predicate> newList = new ArrayList<Predicate>(oldList);
					newList.add(predicate);
					newMatrix.add(newList);
				}
			}
			oldMatrix = newMatrix;
		}
		// Third stage: combine each distinct combination in a conjunction,
		// if there is more than one predicate in the combination.  If there is
		// zero or one predicate, do nothing, or add the predicate itself.
		Set<Predicate> result = new LinkedHashSet<Predicate>();
		for (List<Predicate> terms : oldMatrix) {
			if (terms.size() < 2)
				result.addAll(terms);
			else {
				Proposition proposition = new Proposition("and");
				for (Predicate predicate : terms) {
					proposition.addExpression(predicate);
				}
				result.add(proposition);
			}
		}
		// Fourth stage: filter out any self-inconsistent predicates.
		Notice parInfo = new Notice("Valid partitions of the input/memory space:");
		Notice rejInfo = new Notice("Invalid partitions of the input/memory space:");
		int parCount = 0;
		int rejCount = 0;
		Set<Predicate> filtered = new LinkedHashSet<Predicate>();
		for (Predicate predicate : result) {
			if (predicate.isConsistent()) {
				//DEBUG
				//System.out.println("consistent: " + predicate);
				++parCount;
				parInfo.addNotice(new Analysis("input " + parCount + 
						" = " + predicate.toString()));
				filtered.add(predicate);
			}
			else {
				//DEBUG
				//System.out.println("not consistent: " + predicate);
				++rejCount;
				rejInfo.addNotice(new Analysis("invalid " + rejCount +
						" = " + predicate.toString()));
			}
		}
		if (parCount > 0)
			opInfo.addNotice(parInfo);
		else
			opInfo.addNotice(new Notice("No partitioning of the input/memory space"));
		if (rejCount > 0)
			opInfo.addNotice(rejInfo);
		return filtered;
	}
	
	/**
	 * Checks that the Input values of this Operation are bound to some 
	 * suggested values in the Binding clause.  Issues Warnings if some 
	 * Inputs are not bound.
	 * @param topInfo the top-level Notice for the Protocol.
	 * @param opInfo the local Notice for this Operation.
	 */
	protected void checkBindings(Notice topInfo, Notice opInfo) {
		for (Scenario scenario : getScenarios()) {
			Set<Input> inputs = getInputs();  // Create local set of Inputs.
			Binding binding = scenario.getBinding();
			if (binding != null) {
				// Remove from the set if Input is the target of an Assignment.
				for (Assignment assign : binding.getAssignments()) {
					inputs.remove(assign.operand(0));
				}
			}
			Warning topWarn = null;
			for (Input input : inputs) {
				// If Inputs remain unbound, issue global Warning for this 
				// Operation; and issue a local Warning for each Scenario.
				if (topWarn == null) {
					topWarn = new Warning(
							"Some inputs are not bound in operation: " 
						+ this);
					topInfo.addNotice(topWarn);
				}
				opInfo.addNotice(new Warning("Input: " + input 
						+ " is not bound in scenario: " + scenario));
			}
		}
	}
	
	/**
	 * Returns a printed representation of this Operation.  Returns a String
	 * in the format "name(arg1, ... argN)", representing an invocation of
	 * this Operation.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder(name);
		buffer.append('(');
		int count = 0;
		for (Input input : getInputs()) {
			if (count > 0)
				buffer.append(", ");
			buffer.append(input);
			++count;
		}
		buffer.append(')');
		return buffer.toString();
	}

}
