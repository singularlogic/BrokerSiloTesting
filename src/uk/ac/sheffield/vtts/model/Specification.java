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
 * Specification is the abstract ancestor of different kinds of specification.
 * Specification is the ancestor of the Service and TestSuite specifications.
 * Specification defines the common attributes testDepth and multiTest.  The
 * testDepth attribute represents the maximum path length of sequences to 
 * explore when generating tests; and the multiTest attribute is a boolean
 * flag determining whether to test multiple objectives per test, and so 
 * compress the size of the resulting TestSuite. 
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public abstract class Specification extends Annotated {
	
	/**
	 * The maximum path length of test sequences to explore from each
	 * state.
	 */
	protected int testDepth = 0;
	
	/**
	 * A flag determining whether to verify multiple objectives per test.
	 */
	protected boolean multiTest = false;

	/**
	 * Creates a default unnamed Specification.
	 */
	public Specification() {
	}

	/**
	 * Creates a named Specification.  A Specification must be named with
	 * the name of the type representing the whole System Under Test (SUT).
	 * This name may be used during grounding, to generate an instance of the
	 * SUT to test.
	 * @param name the name of this Specification.
	 */
	public Specification(String name) {
		super(name);
	}
	
	/**
	 * Returns the name of a Specification.  It is an error if the name has
	 * not already been set, when this access-method is invoked.
	 * @return the name.
	 */
	@Override
	public String getName() {
		if (name == null)
			semanticError("must be named for grounding purposes.");
		return name;
	}

	/**
	 * Reports the maximum bounded path length of test sequences to explore
	 * from each state.
	 * @return the testDepth
	 */
	public int getTestDepth() {
		return testDepth;
	}

	/**
	 * Sets the maximum bounded path length of test sequences to explore from
	 * each state.  Ensures that this is set to a non-negative value.
	 * @param testDepth a zero or positive integer value.
	 * @return this Specification.
	 */
	public Specification setTestDepth(int testDepth) {
		this.testDepth = testDepth < 0 ? 0 : testDepth;
		return this;
	}

	/**
	 * Reports whether multiple test objectives are verified per test 
	 * sequence.
	 * @return true, if multiple objectives per test are  to be verified.
	 */
	public boolean isMultiTest() {
		return multiTest;
	}

	/**
	 * Optionally reports whether multiple test objectives are verified per
	 * test sequence.  Only used during unmarshalling, to store the optional
	 * multiTest attribute. 
	 * @return true, or null.
	 */
	public Boolean getMultiTest() {
		return multiTest ? true : null;
	}

	/**
	 * Sets whether to verify multiple test objectives per test sequence.
	 * @param multiTest true, if multiple objectives per test are to be 
	 * verified.
	 * @return this Specification.
	 */
	public Specification setMultiTest(boolean multiTest) {
		this.multiTest = multiTest;
		return this;
	}

}
