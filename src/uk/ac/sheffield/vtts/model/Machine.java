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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Machine represents a named finite state Machine.  A Machine is a model of
 * a software Service that describes its reactive behaviour, in which actions
 * are either allowed or prohibited in certain states.  A Machine models this
 * high-level behaviour as a set of States and expected Transitions linking
 * these States.  The number of States depends on the distinct modes in which
 * different subsets of actions are to be allowed.  The Transitions model the
 * expected actions allowed in each State, including any anticipated error
 * handling.  Missing Transitions represent actions that are ignored in that 
 * State, corresponding to null operations.  The name of a Machine should 
 * correspond to the name of the Service owning the Machine.  
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Machine extends Annotated {

	/**
	 * The set of States, indexed by their unique names. 
	 */
	private Map<String, State> states;
	
	/**
	 * The initial State; a volatile variable.  Stores a cached reference to
	 * the first added State that was identified as an initial State.
	 */
	private State initial;
	
	/**
	 * The current State; a volatile variable.  Initially, and after a reset,
	 * this refers to the initial State.  Subsequently, it refers to the
	 * target State reached after firing a Transition.
	 */
	private State current;
	
	/**
	 * The timeout for state-space searches, set to five seconds.
	 */
	public static final int TIMEOUT = 5000;
	
	/**
	 * Creates a default finite state Machine.
	 */
	public Machine() {
		states = new LinkedHashMap<String,State>();
	}
	
	/**
	 * Creates a named finite state Machine.  A Machine may optionally be
	 * named, where any supplied name must then match the owning Service's 
	 * name.
	 * @param name the name of the Machine.
	 */
	public Machine(String name) {
		super(name);
		states = new LinkedHashMap<String,State>();
	}

	/**
	 * Adds a named State to the Machine.  Each State must be uniquely named.
	 * A duplicate State will replace an existing State with the same name.
	 * This method may be used to add all the desired States to a default 
	 * Machine.  Any State declared to be a initial State will be cached as
	 * the initial State, but only one initial State is allowed.  The current
	 * State will also be set to the initial State.
	 * @param state the state to add.
	 * @return this Machine.
	 */
	public Machine addState(State state) {
		states.put(state.getName(), state);
		if (state.isInitial()) {
			if (initial != null)
				semanticError("already has an initial state '" + 
						initial.getName() + "'.");
			initial = state;
			current = state;
		}
		return this;
	}
	
	/**
	 * Returns the unique State with the given name.
	 * @param name the name of the State.
	 * @return the State, or null.
	 */
	public State getState(String name) {
		return states.get(name);
	}
	
	/**
	 * Returns the set of States belonging to this Machine.  Creates a new
	 * set containing all the unique States of this Machine. 
	 * @return the set of States.
	 */
	public Set<State> getStates() {
		return new LinkedHashSet<State> (states.values());
	}
	
	/**
	 * Returns the set of Transitions belonging to this Machine.  Creates a
	 * new set containing all the unique Transitions of this Machine.
	 * @return the set of Transitions.
	 */
	public Set<Transition> getTransitions() {
		Set<Transition> result = new LinkedHashSet<Transition>();
		for (State state : getStates()) {
			result.addAll(state.getTransitions());
		}
		return result;
	}
		
	/**
	 * Returns the initial State.
	 * @return the initial State.
	 */
	public State getInitialState() {
		if (initial == null)
			semanticError("has no initial State.");
		return initial;
	}
	
	/**
	 * Returns the current State;
	 * @return the current State.
	 */
	public State getCurrentState() {
		if (current == null)
			semanticError("has no current State.");
		return current;
	}
	
	/**
	 * Resets this Machine to its initial State.  The current State of this
	 * Machine is reset to the Machine's initial State.  This method is
	 * invoked at the start of executing a fresh Sequence.
	 * @return this Machine.
	 */
	public Machine reset() {
		current = getInitialState();
		current.setCovered(true);
		return this;
	}
	
	/**
	 * Resets every State and Transition in this Machine to its unmarked
	 * status.  Whenever a Transition fires, both the Transition and the
	 * reached State are marked, to indicate that they have been covered.
	 * This method is invoked before a new TestSuite is created, to reset
	 * the traces in the model.
	 * @return this Machine.
	 */
	public Machine clear() {
		for (State state : getStates()) {
			state.setCovered(false);
			for (Transition transition : state.getTransitions()) {
				transition.setCovered(false);
			}
		}
		return this;
	}
	
	/**
	 * Reports whether this Machine accepts an Event in its current State.
	 * Reports whether this Machine's current State has an exit Transition 
	 * labelled with the same name as the Event.
	 * @param event the Event.
	 * @return true, if an enabled Transition exists for this Event.
	 */
	public boolean accept(Event event) {
		return current.getEnabled(event) != null;
	}
	
	/**
	 * Fires an event on this Machine.  Applies the Event to this Machine in
	 * its current State.  If an enabled Transition exists, the Transition is
	 * marked as having been fired, and this Machine's current State is set
	 * to the destination State indicated by the Transition.  If no enabled
	 * Transition exists, does nothing.
	 * @param event the event.
	 * @return true, if the Event is accepted by this Machine.
	 */
	public boolean fireEvent(Event event) {
		Transition transition = getCurrentState().getEnabled(event);
		if (transition != null) {
			transition.setCovered(true);
			String target = transition.getTarget();
			current = getState(target);
			if (current == null)
				semanticError("has no target State named '" + target + "'.");
			current.setCovered(true);
			return true;
		}
		else 
			return false;
	}
	
	/**
	 * Returns the Alphabet of this Machine.  Computes the Alphabet, the set
	 * of Events understood by this Machine.  Iterates over the Transitions
	 * of this Machine and collects the Transition labels uniquely in a set.
	 * @return the Alphabet of this Machine, the set of Events.
	 */
	public Alphabet getAlphabet() {
		Alphabet alphabet = new Alphabet();
		for (Transition transition : getTransitions()) {
			alphabet.addEvent(new Event(transition.getName()));
		}
		return alphabet;
	}

	/**
	 * Calculates the state cover for this Machine.  This is the smallest
	 * Language containing the shortest Sequences that will reach every State
	 * in this Machine.  Performs a breadth-first search, starting with the
	 * empty Sequence, and extending this on each iteration with every Event
	 * from the Alphabet, until every State has been visited once.  Times out
	 * if the search eventually fails to reach every State.
	 * @return the state cover Language.
	 */
	public Language getStateCover() {
		return getStateCover(getAlphabet());
	}
	
	/**
	 * Calculates the n-transition cover for this Machine.  This is a Language
	 * consisting of Sequences that will reach every State and then explore 
	 * every Transition path of length k = 0..n, starting from each State.
	 * The result includes the state cover, and is computed by taking the
	 * product of the state cover and the bounded language Ln*, which is the
	 * set of all Sequences of length 0..n chosen from the Alphabet.
	 * @param length the maximum Transition path length to explore.
	 * @return the n-transition cover Language.
	 */
	public Language getTransitionCover(int length) {
		if (length < 0) length = 0;
		Alphabet alphabet = getAlphabet();				// the event alphabet
		Language cover = getStateCover(alphabet);		// the state cover
		Language trans = Language.createBoundedStar(alphabet, length);
		return cover.product(trans);					// the transition cover
	}
	
	/**
	 * Calculates the state cover for this Machine.  This is the smallest
	 * Language containing the shortest Sequences that will reach every State
	 * in this Machine.  Performs a breadth-first search, starting with the
	 * empty Sequence, and extending this on each iteration with every Event
	 * from the Alphabet, until every State has been visited once.  Times out
	 * if the search eventually fails to reach every State.
	 * @param alphabet the Alphabet of this Machine.
	 * @return the state cover Language.
	 */
	protected Language getStateCover(Alphabet alphabet) {
		long timeout = System.currentTimeMillis() + TIMEOUT;
		Language stateCover = new Language();		// the state cover
		Set<State> toFind = getStates();			// the states to find
		List<Sequence> paths = new ArrayList<Sequence>(); // paths to explore
		paths.add(new Sequence());
		clear();									// reset any tracer marks
		while (! toFind.isEmpty() && System.currentTimeMillis() < timeout) {
			Sequence sequence = paths.remove(0);
			reset();								// checks for initial state
			for (Event event : sequence.getEvents())
				fireEvent(event);					// either succeeds or fails
			if (toFind.remove(getCurrentState())) {
				sequence.setState(getCurrentState().getName());
				stateCover.addSequence(sequence);
			}
			if (! toFind.isEmpty())
				for (Event event : alphabet.getEvents()) {
					paths.add(new Sequence(sequence).addEvent(event));
				}
		}
		return stateCover;
	}

	/**
	 * Validates the States and Transitions of this Machine for completeness.
	 * Checks for correspondence between the Transitions in this Machine and
	 * the Scenarios in the Protocol.  Checks for the existence of an initial
	 * State and that all States are reachable in the Machine (notwithstanding
	 * any guards in the Protocol).  Checks the completeness of each State 
	 * under all Events from the Alphabet.  Annotates this Machine with 
	 * different Notice, Analysis and Warning nodes.
	 * @param protocol the associated Protocol, for comparison.
	 * @return this annotated Machine.
	 */
	public Machine validateMachine(Protocol protocol) {
		String name = getName() == null ? "unnamed" : getName();
		Notice topInfo = 
				new Notice("Validation report for machine: " + name);
		addNotice(topInfo);
		Alphabet alphabet = getAlphabet();
		Alphabet actions = protocol.getAlphabet();
		checkDifference(topInfo, actions.subtract(alphabet));
		try {
			getStateCover(alphabet);  // Exercise for reachability
		}
		catch (SemanticError error) {
			checkInitialState(topInfo);
			checkTargetStates(topInfo);
		}
		for (State state : getStates()) {
			state.checkCompleteness(topInfo, alphabet);
		}
		return this;
	}
	
	/**
	 * Checks the difference between the Protocol alphabet and the Machine 
	 * alphabet.  If the Protocol alphabet contains more Events, creates a
	 * Warning describing which Events are not handled by this Machine, and
	 * attaches this Warning to the topInfo Notice.
	 * @param topInfo the top-level Notice.
	 * @param difference the extra actions present in the Protocol alphabet.
	 * @return a Warning, or null.
	 */
	protected Warning checkDifference(Notice topInfo, Alphabet difference) {
		Warning warning = null;
		if (! difference.isEmpty()) {
			warning = new Warning(
					"Machine does not handle the Protocol actions:");
			topInfo.addNotice(warning);
			for (Event event : difference.getEvents()) {
				warning.addElement(event);
			}
		}
		return warning;
	}
	
	/**
	 * Checks that this Machine has an initial State.  If this Machine has no
	 * initial State, creates a Warning describing how this Machine cannot be
	 * simulated, and attaches this Warning to the topInfo Notice.
	 * @param topInfo the top-level Notice.
	 * @return a Warning, or null.
	 */
	protected Warning checkInitialState(Notice topInfo) {
		for (State state : getStates()) {
			if (state.isInitial()) 
				return null;
		}
		Warning warning = new Warning(
				"Machine has no initial state; cannot simulate");
		topInfo.addNotice(warning);
		return warning;
	}
	
	/**
	 * Checks that the Transitions of this Machine refer to properly-named 
	 * target States.  If any Transition refers to a non-existent target
	 * State, creates a Warning describing how that State does not exist,
	 * and attaches this Warning to the topInfo Notice.
	 * @param topInfo the top-level Notice.
	 * @return a Warning, or null.
	 */
	protected Warning checkTargetStates(Notice topInfo) {
		Warning warning = null;
		for (Transition transition : getTransitions()) {
			String target = transition.getTarget();
			if (! states.containsKey(target)) {
				String label = transition.getName();
				warning = new Warning("Transition " + label 
						+ " has bad target State: " + target);
				topInfo.addNotice(warning);
			}
		}
		return warning;
	}
	
	/**
	 * Checks the coverage of States after simulating this Machine to find 
	 * the state cover.  If one or more States were never reached, creates a
	 * Warning describing which States were never reached, and attaches this
	 * Warning to the topInfo Notice.
	 * @param topInfo the top-level Notice.
	 * @return a Warning, or null.
	 */
	public Warning checkStateCoverage(Notice topInfo) {
		Warning warning = null;
		for (State state : getStates()) {
			if (! state.isCovered()) {
				if (warning == null) {
					warning = new Warning("These States were never reached:");
					topInfo.addNotice(warning);
				}
				warning.addElement(state.snapshot());  // copy without transitions
			}
		}
		return warning;
	}
	
	/**
	 * Checks the coverage of Transitions after simulating this Machine with a
	 * Language of a given maximum size.  If one or more Transitions were never
	 * fired, creates a Warning describing which Transitions were never fired, 
	 * and attaches this Warning to the topInfo Notice.
	 * @param topInfo the top-level Notice.
	 * @return a Warning, or null.
	 */
	public Warning checkTransitionCoverage(Notice topInfo) {
		Warning warning = null;
		for (Transition transition : getTransitions()) {
			if (! transition.isCovered()) {
				if (warning == null) {
					warning = new Warning("These Transitions were never fired:");
					topInfo.addNotice(warning);
				}
				warning.addElement(transition);
			}
		}
		return warning;
	}

}
