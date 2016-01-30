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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * State represents a named state in a finite state machine.  A State is
 * named in "CapitalCase" and is uniquely indexed by this name, within its
 * owning Machine.  Each State owns a set of uniquely named exit Transitions,
 * traversing from this State to another State in the same Machine.  One
 * State within a Machine may be marked as the initial State.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class State extends Coverable {
	
	/**
	 * Indicates whether this is a initial-state.  False by default.
	 */
	private boolean initial = false;
	
	/**
	 * The set of labelled Transitions exiting this State.
	 */
	private Map<String, Transition> transitions;

	/**
	 * Creates a default state.
	 */
	public State() {
		transitions = new LinkedHashMap<String, Transition>();
	}
	
	/**
	 * Creates a named State.
	 * @param name the name of this State.
	 */
	public State(String name) {
		super(name);
		transitions = new LinkedHashMap<String, Transition>();
	}
	
	/**
	 * Returns the name of a State.  It is an error if the name has
	 * not already been set, when this access method is invoked.
	 * @return the name.
	 */
	@Override
	public String getName() {
		if (name == null)
			semanticError("must be named for indexing purposes.");
		return name;
	}

	/**
	 * Sets whether this is a initial State.
	 * @param value true, if this is a initial State.
	 * @return this State.
	 */
	public State setInitial(boolean value) {
		initial = value;
		return this;
	}

	/**
	 * Optionally reports whether this is the initial State.  Used during
	 * marshalling, where saving this attribute is optional.
	 * @return the Boolean object true, or null.
	 */
	public Boolean getInitial() {
		return initial ? initial : null;
	}

	/**
	 * Reports whether this is the initial State.
	 * @return true if this is the initial State.
	 */
	public boolean isInitial() {
		return initial;
	}
	
	/**
	 * Adds a named exit Transition to this State.  Each Transition must be 
	 * uniquely named.  A duplicate Transition will replace an existing 
	 * Transition with the same name.  The Transition's source must match the
	 * name of this State.  If no source or target are specified, these are
	 * set to the name of this State.
	 * @param transition the transition to add.
	 * @return this State.
	 */
	public State addTransition(Transition transition) {
		if (transition.getSource() == null)
			transition.setSource(getName());
		if (transition.getTarget() == null)
			transition.setTarget(getName());
		String source = transition.getSource();
		if (! source.equals(getName()))
				semanticError("cannot add a Transition with source '" 
						+ source + "'.");
		transitions.put(transition.getName(), transition);
		return this;
	}
	
	/**
	 * Returns the unique exit Transition with the given name.
	 * @param name the name of the Transition.
	 * @return the named Transition, or null.
	 */
	public Transition getTransition(String name) {
		return transitions.get(name);
	}
	
	/**
	 * Returns the list of exit Transitions for this State.
	 * @return the list of exit Transitions.
	 */
	public List<Transition> getTransitions() {
		return new ArrayList<Transition> (transitions.values());
	}

	/**
	 * Returns the exit Transition whose name matches a given event.  
	 * @param event the event.
	 * @return the Transition matching the event, or null.
	 */
	public Transition getEnabled(Event event) {
		return transitions.get(event.getName());
	}

	/**
	 * Takes a snapshot of this State, without its dependent Transitions.
	 * This is used when generating a Warning about this State, to report
	 * that it has not been covered.
	 * @return a clone of this State, without its Transitions.
	 */
	public State snapshot() {
		State result = new State(getName());
		result.setInitial(isInitial());
		return result;
	}
	
	/**
	 * Checks the completeness of this State with respect to an Alphabet.
	 * Creates a Notice for this State describing its completeness with
	 * respect to the given Alphabet.  Attaches a Warning if this State is 
	 * unreachable (after simulation of its Machine); and an Analysis listing
	 * any Events that are ignored.  Also attaches summary Warning and 
	 * Analysis nodes to the topInfo Notice.
	 * @param topInfo the top-level Notice.
	 * @param alphabet the Alphabet of the associated Machine.
	 * @return the Notice describing the completeness check.
	 */
	public Notice checkCompleteness(Notice topInfo, Alphabet alphabet) {
		Notice stateInfo = new Notice(
				"Completeness check for state: " + name);
		addNotice(stateInfo);
		if (! isCovered()) {
			stateInfo.addNotice(new Warning("State is not reachable"));
			topInfo.addNotice(
					new Warning("State is not reachable: " + name));
		}
		Analysis ignored = null;
		for (Event event : alphabet.getEvents()) {
			if (getEnabled(event) == null) {
				if (ignored == null) {
					ignored = new Analysis("State ignores the events:");
					stateInfo.addNotice(ignored);
					topInfo.addNotice(new Analysis(
							"Events are ignored in State: " + name));
				}
				ignored.addElement(event);
			}
		}
		return stateInfo;
	}

}
