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

package uk.ac.sheffield.vtts.ground;

import uk.ac.sheffield.vtts.model.TestSequence;
import uk.ac.sheffield.vtts.model.TestStep;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * Grounding is the abstract interface satisfied by concrete test generators.
 * A Grounding is any class implementing this interface, which encapsulates an
 * algorithm for transforming a high-level TestSuite into a concrete test 
 * suite (e.g. JUnit, or SOAP, or REST ...) that can be executed in some live
 * environment.  The Grounding interface is designed according to the Visitor
 * Design Pattern.  Each of TestSuite, TestSequence and TestStep may receive
 * a Grounding object, which then provides the algorithm to ground that kind
 * of node.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public interface Grounding {
	
	/**
	 * Top level grounding method.  This should generate code for the entire
	 * TestSuite.  The method should open and close the file that contains the
	 * resulting generated concrete test suite; and ask every TestSequence in 
	 * the TestSuite to receive this Grounding, in order to generate the rest
	 * of the grounded test suite.
	 * @param testSuite the high-level test suite.
	 */
	public abstract void groundTestSuite(TestSuite testSuite);
	
	/**
	 * Intermediate grounding method.  This should generate code for a single
	 * TestSequence.  This method should generate the skeleton code for the 
	 * TestSequence and then ask every TestStep contained in this TestSequence
	 * to receive this Grounding, in order to generate the instruction steps
	 * in the grounded test sequence.
	 * @param sequence the high-level test sequence.
	 */
	public abstract void groundTestSequence(TestSequence sequence);
	
	/**
	 * Low level grounding method.  This should generate code for a single 
	 * TestStep.  This method should convert the operation call into concrete
	 * code with concrete arguments and generate a variable to hold the result.
	 * If the TestStep is to be verified, this method should generate suitable
	 * assertions that check (1) that the triggered operation was the expected
	 * one; (2) that the reached state was the expected one; and (3) that any
	 * return value was equal to the expected output.
	 * @param testStep the high-level test step.
	 */
	public abstract void groundTestStep(TestStep testStep);
	
	/**
	 * Sets the name of the target package for the generated Test Driver class.
	 * @param packageInfo the Test Driver package name.
	 */
	public abstract void useTargetPackage(String packageInfo);
	
	/**
	 * Sets the name of the source package for the System-Under-Test.  The first
	 * such package (if added) is used as the location of the top-level SUT class.
	 * Any further added packages correspond to places where other user-defined
	 * types may be found.
	 * @param packageInfo the Service-Under-Test package name.
	 */
	public abstract void useSourcePackage(String packageInfo);
	
	/**
	 * Tells this Grounding to use the given root URI as the endpoint for the
	 * service (optional).  This is used mainly by REST or SOAP groundings that 
	 * need to supply the endpoint.  Some groundings may have encapsulated the
	 * endpoint in the service client, so will not need this.
	 * @param serviceURI the root URI of the service.
	 */
	public abstract void useEndpoint(String serviceURI);

	/**
	 * Selects whether to test the full state and transition behaviour (when
	 * true), or just the return values (when false) of the tested service.
	 * When true, the service must provide additional test-mode operations that
	 * reveal its states and the last transition fired.
	 * @param value true, or false.
	 */
	public abstract void setMetaCheck(boolean value);
	
}
