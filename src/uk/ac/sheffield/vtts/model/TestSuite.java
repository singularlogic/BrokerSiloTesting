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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.sheffield.vtts.ground.Grounding;

/**
 * TestSuite represents a set of generated high-level test testSequences.  A
 * TestSuite is the more concrete counterpart of a Language, which contains
 * testSequences of events.  A TestSuite contains testSequences of tests, populated
 * with the inputs, outputs, conditions and effects.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class TestSuite extends Specification {
	
	/**
	 * The set of TestSequences that constitute this TestSuite.  These are
	 * presented in the order in which they should be executed, to ensure
	 * that prior properties are verified before the same steps are used as
	 * part of the set-up for longer sequences.
	 */
	private Set<TestSequence> testSequences;
	
	/**
	 * The grounding requested for this TestSuite, set to "Java" by default,
	 * as if the service-under-test is a plain old Java object (POJO).
	 */
	private String grounding = "Java";
	
	/**
	 * A flag indicating whether to verify full state and transition 
	 * behaviour, set to true by default.
	 */
	private boolean metaCheck = true;
	
	/**
	 * Creates an empty TestSuite.
	 */
	public TestSuite() {
		testSequences = new LinkedHashSet<TestSequence>();
	}
	
	/**
	 * Creates an empty TestSuite for the given system and test depth.
	 * @param name the name of the System Under Test (SUT).
	 * @param depth the desired maximum depth for testing.
	 */
	public TestSuite(String name, int depth) {
		super(name);
		this.testDepth = depth;
		testSequences = new LinkedHashSet<TestSequence>();
	}
	
	/**
	 * Returns the name of the System Under Test (SUT).  This is the same as
	 * the name of the Service for which this TestSuite was generated, and is
	 * the same as the name of this TestSuite.  This name may be used during
	 * grounding, to create an instance of the system type corresponding to
	 * this name.
	 * @return the name of the System Under Test (SUT)
	 */
	public String getSystem() {
		return name;
	}
	
	/**
	 * Returns the name of the Test Driver (TD) for the System Under Test  
	 * (SUT).  The name of the TD is conventionally created by appending the
	 * name of the SUT and "Test".  This name may be used during grounding, 
	 * to create an instance of the test driver type corresponding to this 
	 * name.
	 * @return the name of the Test Driver (TD).
	 */
	public String getTestDriver() {
		return name + "Test";
	}
	
	/**
	 * Returns the size of this TestSuite.  Counts the number of TestSequences
	 * in this TestSuite.
	 * @return the number of TestSequences in this TestSuite.
	 */
	public int size() {
		return testSequences.size();
	}

	/**
	 * Returns the set of TestSequences that constitute this TestSuite.  These
	 * are in the order in which the tests should be executed, which ensures
	 * that earlier properties are verified before they are used as part of
	 * the set-up in longer TestSequences.
	 * @return a set of TestSequences.
	 */
	public Set<TestSequence> getTestSequences() {
		return testSequences;
	}
	
	/**
	 * Adds a sequence to the set of TestSequences in this TestSuite.  If this
	 * TestSuite does not already contain a TestSequence equal to the added
	 * TestSequence, it includes the new sequence in its set of TestSequences.
	 * Sets the test index of the added TestSequence (using 1..n indexing).
	 * @param sequence the TestSequence to add.
	 * @return this TestSuite.
	 */
	public TestSuite addTestSequence(TestSequence sequence) {
		if (testSequences.add(sequence))
			sequence.setTest(testSequences.size());
		return this;
	}
	
	/**
	 * Returns the grounding specified for this TestSuite, if any.  This is a
	 * symbolic String whose value denotes a particular kind of grounding.
	 * Examples might be:  "Java", or "JAX-WS", or "JAX-RS".
	 * @return the grounding, or null.
	 */
	public String getGrounding() {
		return grounding;
	}

	/**
	 * Sets the grounding desired for this TestSuite, if any.  This is a
	 * symbolic String whose value denotes a particular kind of grounding.
	 * Examples might be:  "Java", or "JAX-WS", or "JAX-RS".
	 * @param grounding the symbolic grounding String.
	 * @return this TestSuite.
	 */
	public TestSuite setGrounding(String grounding) {
		this.grounding = grounding;
		return this;
	}

	/**
	 * Reports whether meta-checks of full state and transition behaviour
	 * are to be performed.  True, by default.  This should be set to false
	 * if the tested service does not offer access to its full state and
	 * transition behaviour in test-mode.
	 * @return true, or false.
	 */
	public boolean isMetaCheck() {
		return metaCheck;
	}
	
	/**
	 * Optionally reports whether meta-checks of full state and transition
	 * behaviour are to be performed.    Only used during unmarshalling, to
	 * store the optional metaCheck attribute. 
	 * @return true, or null.
	 */
	public Boolean getMetaCheck() {
		return metaCheck ? true : null;
	}

	/**
	 * Sets whether to perform meta-checks of full state and transition
	 * behaviour.  Set to true (the default), if the tested service offers
	 * meta-access to its full state and transition behaviour; otherwise 
	 * set to false.
	 * @param metaCheck true, if meta-checks are to be performed.
	 */
	public void setMetaCheck(boolean metaCheck) {
		this.metaCheck = metaCheck;
	}

	/**
	 * Compress this TestSuite, returning a more compact TestSuite in which
	 * single TestSequences may verify multiple test objectives.  Without any
	 * compression, each TestSequence consists of a number of TestSteps that
	 * are part of the set-up, followed by a verified TestStep which is the
	 * test objective.  Each TestSequence is by default a single-objective
	 * test.  With compression, any TestSequence that is a prefix of some
	 * longer TestSequence is merged with that sequence, resulting in some
	 * TestSequences that verify multiple test objectives.  The resulting
	 * smaller TestSuite is re-ordered, so that the properties of shorter
	 * paths are verified before the longer paths of which they are the
	 * prefix.  The multiTest attribute is set to true.
	 * @return the size of this compressed TestSuite.
	 */
	public int compress() {
		List<TestSequence> result = new ArrayList<TestSequence>();
		for (TestSequence shorter : getTestSequences()) {
			boolean merged = false;
			for (TestSequence longer : getTestSequences()) {
				if (merged = longer.mergePrefix(shorter)) 
					break;
			}
			if (! merged)
				result.add(shorter);
		}
		Collections.sort(result);  // By order of test priority
		multiTest = true;
		testSequences = new LinkedHashSet<TestSequence>();
		for(TestSequence test : result) {
			testSequences.add(test);
			test.setTest(testSequences.size());
		}
		return size();
	}
	
	/**
	 * Receive a Grounding generator for grounding this TestSuite.  The actual
	 * generator is any class that implements the Grounding interface.  Asks
	 * the grounding reciprocally to ground this TestSuite.
	 * @param grounding a Grounding generator.
	 */
	public void receive(Grounding grounding) {
		grounding.groundTestSuite(this);
	}

}
