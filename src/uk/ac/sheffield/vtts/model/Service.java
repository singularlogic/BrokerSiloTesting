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
import java.util.List;
import java.util.Set;

/**
 * Service represents a a model or specification of a software service. 
 * The model consists of two parts, a Protocol, describing the Memory and
 * Operations of the Service, and a Machine, describing the States and
 * Transitions of the Service.  Both aspects are used to simulate the
 * behaviour of the Service.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Service extends Specification {
		
	/**
	 * The timeout for state-space searches, set to five seconds.
	 */
	public static final int TIMEOUT = 5000;

	/**
	 * The Protocol of this Service.  The Protocol describes the global 
	 * Memory and set of Operations for the Service.  It describes the
	 * detailed abstract behaviour of each Operation, in terms of its
	 * Inputs, Outputs, Failures and alternative Scenarios.
	 */
	private Protocol protocol;
	
	/**
	 * The finite state Machine for this service.  The Machine models the
	 * state transition diagram for the Service.  In particular, it models
	 * modes in which particular Operations are available, or unavailable. 
	 */
	private Machine machine;
	
	/**
	 * Creates a default Service with no name.
	 */
	public Service() {
	}
	
	/**
	 * Creates a named Service.  The name is supplied in "CapitalCase", and
	 * should correspond to the type name of the unit representing the whole
	 * service.  In a Java grounding, this would be the name of the class or
	 * interface under test.
	 * @param name the name of this Service.
	 */
	public Service(String name) {
		super(name);
	}
	
	/**
	 * Adds the Protocol to this service.  The Protocol describes the global 
	 * Memory and set of Operations for the Service.  It describes the
	 * detailed abstract behaviour of each Operation, in terms of its
	 * Inputs, Outputs, Failures and alternative Scenarios.  Ensures that
	 * the Protocol is named consistently with this Service.
	 * @param protocol the Protocol to add.
	 * @return this Service.
	 */
	public Service addProtocol(Protocol protocol) {
		protocol.setName(getName(), true);
		this.protocol = protocol;
		return this;
	}
	
	/**
	 * Returns the Protocol of this service.  The Protocol describes the 
	 * global Memory and set of Operations for the Service.  It describes 
	 * the detailed abstract behaviour of each Operation, in terms of its
	 * Inputs, Outputs, Failures and alternative Scenarios.
	 * @return the Protocol of this service.
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Adds the finite state Machine of this Service.  The Machine models
	 * the state transition diagram for the Service.  In particular, it 
	 * models modes in which particular Operations are available, or 
	 * unavailable.  Ensures that the Machine is named consistently with
	 * this Service.
	 * @param machine the finite state Machine to add.
	 * @return this Service.
	 */
	public Service addMachine(Machine machine) {
		machine.setName(getName(), true);
		this.machine = machine;
		return this;
	}
	
	/**
	 * Returns the finite state Machine of this Service.  The Machine models 
	 * the state transition diagram for the Service.  In particular, it 
	 * models modes in which particular Operations are available, or 
	 * unavailable.
	 * @return the finite state Machine of this Service.
	 */
	public Machine getMachine() {
		return machine;
	}
	
	/**
	 * Validates the states and transitions of this Service's state Machine.
	 * Checks for correspondence between the Transitions in the Machine and
	 * the Scenarios in the Protocol.  Checks for the existence of an initial
	 * state and that all states are reachable in the Machine (notwithstanding
	 * any guards in the Protocol).  Checks the completeness of each State 
	 * under all Events from the Alphabet.  Annotates the Machine with 
	 * various Notice, Analysis and Warning nodes.
	 * @return the annotated Machine.
	 */
	public Machine validateMachine() {
		return machine.validateMachine(protocol);
	}
	
	/**
	 * Verifies this Service's Protocol for consistency and completeness.
	 * Checks for correspondence between the Scenarios in the Protocol and 
	 * the Transitions in the Machine.  Checks that the Memory is correctly 
	 * initialised.  Checks that each Operation is deterministic and complete
	 * under all input and memory conditions.  Annotates the Protocol with
	 * various Notice, Analysis and Warning nodes.
	 * @return the annotated Protocol.
	 */
	public Protocol verifyProtocol() {
		return protocol.verifyProtocol(machine);
	}
	
	/**
	 * Generates the TestSuite for this Service, using generation parameters
	 * from this Service.  Generates a high-level TestSuite by simulating the
	 * complete Service specification, which is a Stream X-Machine.  Explores
	 * all Transition paths from every State of the Machine, up to a maximum
	 * bounded depth, then filters these paths through the guard Conditions
	 * of the Operations in the Protocol.  Generates positive tests for paths
	 * accepted by both the Machine and the Protocol; and negative tests for
	 * paths accepted by the Protocol, but rejected by the Machine.  Prunes
	 * infeasible paths rejected by the Protocol.  Prunes redundant paths 
	 * with ignored steps in the path prefix.  Optionally compresses the 
	 * resulting TestSuite if multi-objective tests were requested.
	 * @return an optimised TestSuite.
	 */
	public TestSuite generateTests() {
		Alphabet alphabet = machine.getAlphabet();
		Language stateCover = getStateCover(alphabet);
		Language allPaths = Language.createBoundedStar(alphabet, testDepth);
		return generateTestSuite(stateCover.product(allPaths));
	}
	
	/**
	 * Generates the TestSuite for this Service, using the given generation
	 * parameters.  Generates a high-level TestSuite by simulating the
	 * complete Service specification, which is a Stream X-Machine.  Explores
	 * all Transition paths from every State of the Machine, up to a maximum
	 * bounded depth, then filters these paths through the guard Conditions
	 * of the Operations in the Protocol.  Generates positive tests for paths
	 * accepted by both the Machine and the Protocol; and negative tests for
	 * paths accepted by the Protocol, but rejected by the Machine.  Prunes
	 * infeasible paths rejected by the Protocol.  Prunes redundant paths 
	 * with ignored steps in the path prefix.  Optionally compresses the 
	 * resulting TestSuite if multi-objective tests were requested.
	 * @param testDepth the maximum depth of generated test paths.
	 * @param multiTest whether to generate multi-objective tests.
	 * @return an optimised TestSuite.
	 */
	public TestSuite generateTests(int testDepth, boolean multiTest) {
		this.testDepth = testDepth;		// set the new testDepth
		this.multiTest = multiTest;		// set the new multiTest
		Alphabet alphabet = machine.getAlphabet();
		Language stateCover = getStateCover(alphabet);
		Language allPaths = Language.createBoundedStar(alphabet, testDepth);
		return generateTestSuite(stateCover.product(allPaths));
	}

	/**
	 * Calculates the state cover for this Service.  This is the smallest 
	 * Language containing the shortest Sequences that will reach every
	 * State in this Service's Machine, via Transitions whose guard 
	 * conditions are also satisfied in this Service's Protocol.  Performs
	 * a breadth-first search, starting with the empty Sequence, and 
	 * extending this on each iteration with every Event from the Alphabet,
	 * until every State has been visited once.  Times out if the search 
	 * eventually fails to reach every State.
	 * @param alphabet the Alphabet for this Service's Machine.
	 * @return the state cover Language for this Service.
	 */
	protected Language getStateCover(Alphabet alphabet) {
		long timeout = System.currentTimeMillis() + TIMEOUT; // set the timeout
		Language stateCover = new Language();
		Set<State> toFind = machine.getStates();			// the states to find
		List<Sequence> paths = new ArrayList<Sequence>();	// paths to explore
		paths.add(new Sequence());							// contains empty sequence
		while (! toFind.isEmpty() && System.currentTimeMillis() < timeout) {
			// Explore one sequence from the search space
			machine.reset();
			protocol.reset();
			Sequence sequence = paths.remove(0);
			boolean expand = true;						// expand empty sequence
			for (Event event : sequence.getEvents()) {
				boolean enabled = machine.accept(event);
				boolean triggered = protocol.accept(event);
				if (enabled && triggered) {
					machine.fireEvent(event);
					protocol.fireEvent(event);
					expand = true; 						// expand successful path
				}
				else
					expand = false;						// prune unsuccessful path
			}
			// Check if we found a new state; and if so, remember the sequence
			State currentState = machine.getCurrentState();
			if (toFind.remove(currentState)) {
				sequence.setState(currentState.getName());
				stateCover.addSequence(sequence);
			}
			// If still searching, expand the current successful path
			if (expand && !toFind.isEmpty())
				for (Event event : alphabet.getEvents()) {
					paths.add(new Sequence(sequence).addEvent(event));
				}
		}
		return stateCover;
	}

	/**
	 * Generates the TestSuite for this Service, by simulating the Service
	 * using a bounded Language.  Presents the Language to the Machine and 
	 * Protocol to see whether each Sequence is accepted or rejected.  A 
	 * Sequence that is accepted by both the Machine and the Protocol yields
	 * a positive test case; a Sequence accepted by the Protocol and rejected
	 * by the Machine yields a negative test case; a Sequence rejected by the
	 * Protocol is infeasible under the memory and input conditions, so is
	 * pruned; and a Sequence containing ignored steps in the prefix is 
	 * redundant, so is pruned.  If multi-objective tests were requested,
	 * merges shorter tests with longer tests of which they are the prefix.
	 * @param language the Language used to simulate this Service.
	 * @return the TestSuite specified by the generation parameters.
	 */
	protected TestSuite generateTestSuite(Language language) {
		TestSuite testSuite = new TestSuite(getName(), testDepth);
		Notice topInfo = 
				new Notice("Generated test suite for service: " + getName());
		testSuite.addNotice(topInfo);
		topInfo.addNotice(
				new Analysis("Exploring all paths up to length: " + testDepth));
		int theoretical = language.size();  // Theoretical number of sequences
		topInfo.addNotice(
				new Analysis("Number of theoretical sequences: " + theoretical));
		// Count 
		int redundant = 0;
		int infeasible = 0;
		machine.clear();  	// Remove all traces of execution during State cover
		for (Sequence sequence : language.getSequences()) {
			TestSequence testSequence = generateTestSequence(sequence);
			if (testSequence == null) 
				++infeasible;	// No sequence generated if infeasible
			else {
				int priorSize = testSuite.size();
				testSuite.addTestSequence(testSequence);
				if (testSuite.size() == priorSize)
					++redundant;	// Truncated sequence is redundant
			}
		}
		int actual = testSuite.size();  // Actual number of feasible sequences
		topInfo.addNotice(
				new Analysis("Number of infeasible sequences: " + infeasible));
		topInfo.addNotice(
				new Analysis("Number of redundant sequences: " + redundant));
		topInfo.addNotice(
				new Analysis("Number of executable sequences: " + actual));
		if (multiTest) {
			int compressed = testSuite.compress();  // also indexes sequences
			topInfo.addNotice(new Analysis(
					"Number of multi-objective sequences: " + compressed));
		}
		analyseCoverage(topInfo);
		return testSuite;
	}
	
	/**
	 * Generates a single TestSequence from an abstract Sequence of Events.
	 * If the abstract Sequence is accepted by the Protocol and the Machine,
	 * generates a positive case.  If the Sequence is accepted by the Protocol
	 * but refused by the Machine, generates a negative case.  If the Sequence
	 * is refused by the Protocol, it is infeasible and so no TestSequence can 
	 * be generated.  This is valid, since any complementary Sequence accepted 
	 * by the Protocol will also constitute a successful negative test of the 
	 * infeasible Sequence.  TestSequences are truncated after the first
	 * refusal, to avoid generating TestSequences with nullops in the prefix.
	 * @param sequence the Sequence of Events.
	 * @return a TestSequence, or null.
	 */
	protected TestSequence generateTestSequence(Sequence sequence) {
		machine.reset();
		protocol.reset();
		TestSequence testSequence = new TestSequence(sequence);
			// Standard initial transition, an empty test step
		TestStep initialStep = new TestStep(TestStep.initialName);
		initialStep.setState(machine.getInitialState().getName());
		testSequence.addEvent(initialStep);
			// Append sequence of TestSteps, according to the path length
		for (Event event : sequence.getEvents()) {
			TestStep testStep = generateTestStep(event);
			if (testStep == null)  // infeasible - delete TestSequence
				return null;
			else
				testSequence.addEvent(testStep);
			if (testStep.isRefused())
				break;  // truncate TestSequence after first refusal
		}
		testSequence.doVerify();  // assert outcome of the TestSequence
		return testSequence;
	}
	
	/**
	 * Generates a single TestStep from an abstract Event.  If the Event is
	 * accepted both by the Protocol and the Machine, fires the event and
	 * creates a positive TestStep (to be accepted).  If the Event is only
	 * accepted by the Protocol, but refused by the Machine, generates a
	 * negative TestStep (to be ignored).  If the Protocol refuses the Event,
	 * it is infeasible given the current memory and inputs, so no TestStep
	 * is created.  The TestStep is labelled with the reached State (after
	 * firing) or the unchanged State (after refusal).
	 * @param event the Event.
	 * @return a TestStep, or null.
	 */
	protected TestStep generateTestStep(Event event) {
		Operation operation = protocol.getOperation(event);
		TestStep testCase = new TestStep(event.getName());
		boolean enabled = machine.accept(event);
		boolean triggered = protocol.accept(event);
		if (triggered && enabled) {
			// Protocol and Machine both accept the event.
			machine.fireEvent(event);
			protocol.fireEvent(event);
			testCase.recordInputs(operation);
			testCase.recordResults(operation);  // Return any valid outputs
		}
		else if (triggered && !enabled) {
			// Machine refused the event as a missing transition.  The test
			// case is still feasible, given the input and memory.
			testCase.recordRefusal();
			testCase.recordInputs(operation);
			testCase.recordResults(operation);
		}
		else  // Protocol (and possibly Machine) refused the event.  The test
			  // case cannot be triggered with this input, so is infeasible.
			return null;
		testCase.setState(machine.getCurrentState().getName());
		return testCase;
	}

	/**
	 * Checks whether all the States and Transitions were covered by the
	 * generated TestSuite and adds suitable Warning nodes if any States, or
	 * Transitions, were not covered by the TestSuite.  
	 * @param topInfo the top-level Notice.
	 */
	protected void analyseCoverage(Notice topInfo) {
		Warning stateWarning = machine.checkStateCoverage(topInfo);
		Warning transWarning = machine.checkTransitionCoverage(topInfo);
		if (stateWarning != null || transWarning != null) {
			Warning coverWarning = new Warning(
					"Specification is not fully covered by the test suite");
			if (testDepth < 3)
				coverWarning.addNotice(
						new Analysis("Suggest increasing the path length"));
			else
				coverWarning.addNotice(
						new Analysis("Suggest introducing a new scenario"));
			topInfo.addNotice(coverWarning);
		}
	}
	
	
}

