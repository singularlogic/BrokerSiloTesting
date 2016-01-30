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

import uk.ac.sheffield.vtts.ground.Grounding;

/**
 * TestSequence represents a test whose steps are to be executed in order.
 * A TestSequence is the more concrete realisation of an abstract Sequence,
 * whose symbolic Events are realised as concrete TestSteps.  A TestSequence
 * contains a list of TestSteps, representing a sequence of operations to 
 * invoke on the system under test.  The prefix TestSteps represent set-up
 * instructions and the final TestStep is the verified step, which must be
 * checked using assertions.  A TestSequence records the system State that
 * it is testing, and the Transition path length that it is testing.  It is
 * also indexed with a test-number.
 * <p>
 * A TestSequence may optionally be merged with shorter TestSequences that
 * are a prefix of this TestSequence.  In this case, multiple TestSteps may
 * be verified, and this TestSequence represents a multi-objective test.
 * TestSequences are comparable objects that can be sorted according to their
 * precedence during testing.  By default, shorter TestSequences precede 
 * longer tests; but multi-objective TestSequences precede other tests
 * if they verify more objectives.  This property ensures that testing is 
 * properly ordered, such that fundamental system properties are tested
 * before dependent properties.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class TestSequence extends Sequence implements Comparable<TestSequence> {
	
	/**
	 * The test index of this TestSequence.  A convenient numbering for the
	 * TestSequence, used to align it with generated code.
	 */
	private int test;

	/**
	 * Constructs an empty TestSequence.
	 */
	public TestSequence() {
		super();
	}
	
	/**
	 * Constructs an empty TestSequence covering the same state and path as
	 * the coverage Sequence.
	 * @param sequence a coverage Sequence.
	 */
	public TestSequence(Sequence sequence) {
		super();
		state = sequence.state;
		path = sequence.path;
	}

	/**
	 * Returns the test index of this TestSequence.
	 * @return the test index.
	 */
	public int getTest() {
		return test;
	}

	/**
	 * Sets the test index of this TestSequence.
	 * @param test the test index to set.
	 * @return this TestSequence.
	 */
	public TestSequence setTest(int test) {
		this.test = test;
		return this;
	}
	
	/**
	 * Returns the ordered list of TestSteps.  Provided for convenience, since
	 * the inherited method returns an ordered list of Events.
	 * @return the list of TestSteps.
	 */
	public List<TestStep> getTestSteps() {
		List<TestStep> result = new ArrayList<TestStep>();
		for (Event event : events) 
			result.add((TestStep) event);
		return result;
	}
	
	/**
	 * Reports whether this TestSequence can possibly raise anticipated 
	 * Exceptions.  Checks each TestStep to see whether its invoked Operation
	 * has a signature that can possibly return one or more Failures.  If so,
	 * checks each Failure to see if it is bound (representing a required
	 * exception that will be caught) or unbound (representing an uncaught
	 * exception).  Returns true if there is any uncaught exception.
	 * @return true if this TestSequence can raise anticipated Exceptions.
	 */
	public boolean hasFailures() {
		for (TestStep testStep : getTestSteps()) {
			Operation operation = testStep.getOperation();
			for (Failure failure : operation.getFailures()) {
				if (! failure.isBound())
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Reports whether this TestSequence contains the other as a prefix.
	 * Returns true if the other TestSequence is a prefix of this one, that
	 * is, if its whole sequence of Events is equal to some initial sequence
	 * of Events in this TestSequence.  Returns false immediately if the two
	 * TestSequences are identical, or if the other is not shorter than this
	 * TestSequence.  Otherwise, compares Events pair-wise according to the
	 * definition of Event equality.
	 * @param other the other TestSequence
	 * @return true, if the other is a prefix of this TestSequence.
	 */
	public boolean hasPrefix(TestSequence other) {
		if (this == other || size() <= other.size())
			return false;
		else {
			List<Event> prefix = other.getEvents();
			for (int i = 0; i < prefix.size(); ++i)
				if (! events.get(i).equals(prefix.get(i)))
					return false;
			return true;
		}
	}
	
	/**
	 * Optionally merges a prefix TestSequence with this TestSequence.  If 
	 * the candidate prefix is in fact a prefix of this TestSequence, copies
	 * its verified obligations across to this TestSequence and returns true;
	 * otherwise returns false.  If the merge succeeds, this TestSequence 
	 * becomes a multi-objective test, with more than one verified TestStep,
	 * which also fulfils the verified obligations of the merged TestSequence.
	 * @param prefix the prefix TestSequence.
	 * @return true if the prefix was merged.
	 */
	public boolean mergePrefix(TestSequence prefix) {
		boolean result = hasPrefix(prefix);
		if (result) {
			List<TestStep> prefixTests = prefix.getTestSteps();
			List<TestStep> mainTests = getTestSteps();
			for (int index = 0; index < prefix.size(); ++index) {
				if (prefixTests.get(index).isVerify())
					mainTests.get(index).setVerify(true);
			}
		}
		return result;
	}
	
	/**
	 * Verify the outcome of this TestSequence.  Sets the checked property of
	 * the final TestStep in this TestSequence to true.  Does nothing if this
	 * TestSequence is empty.
	 * @return this TestSequence.
	 */
	public TestSequence doVerify() {
		if (! isEmpty()) {
			TestStep last = (TestStep) getLastEvent();
			last.setVerify(true);
		}
		return this;
	}

	/**
	 * Returns the name of the expected source State.  Returns the name of 
	 * the State reached by the first initialising TestStep.
	 * @return the name of the source State.
	 */
	public String getSource() {
		if (! isEmpty()) {
			TestStep first = (TestStep) getFirstEvent();
			return first.getState();
		}
		else
			return null;
	}

	/**
	 * Returns the name of the expected target State.  Returns the name of
	 * the State reached by the last TestStep in this TestSequence.
	 * @return the name of the target State.
	 */
	public String getTarget() {
		if (! isEmpty()) {
			TestStep last = (TestStep) getLastEvent();
			return last.getState();
		}
		else
			return null;
	}
	
	/**
	 * Counts the number of verified TestSteps in this TestSequence.
	 * @return the number of verified TestSteps.
	 */
	public int countVerify() {
		int count = 0;
		for (TestStep testStep : getTestSteps()) {
			if (testStep.isVerify())
				++count;
		}
		return count;
	}

	/**
	 * Compares one TestSequence with another, as part of a sorting algorithm.
	 * By default, a shorter TestSequence precedes every longer TestSequence,
	 * of which it could be a prefix.  However, when sorting multi-objective
	 * tests, a TestSequence with multiple verified steps should precede a
	 * TestSequence with fewer verified steps, since the sequence with more
	 * verified steps contains its own prefix tests.  This rule takes priority
	 * over the default rule, which applies when two TestSequences have the
	 * same number of verified steps.
	 * @param other the other TestSequence.
	 * @return a negative, zero or positive value, denoting respectively that
	 * this TestSequence precedes, is equal in rank to, or follows the other.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TestSequence other) {
		int myCount = countVerify();
		int otherCount = other.countVerify();
		if (myCount == otherCount)
			return size() - other.size();
		else
			return otherCount - myCount;
	}
	
	/**
	 * Receive a Grounding generator for grounding this TestSequence.  The
	 * actual generator is any class that implements the Grounding interface.
	 * Asks the grounding reciprocally to ground this TestSequence.
	 * @param grounding a Grounding generator.
	 */
	public void receive(Grounding grounding) {
		grounding.groundTestSequence(this);
	}



}
