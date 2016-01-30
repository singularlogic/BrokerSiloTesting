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

import java.io.PrintWriter;
import java.util.Date;

import uk.ac.sheffield.vtts.model.Notice;
import uk.ac.sheffield.vtts.model.Operation;
import uk.ac.sheffield.vtts.model.TestSequence;
import uk.ac.sheffield.vtts.model.TestStep;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * JavaGrounding assumes that the tested artifact is a POJO (plain old Java 
 * object).  It converts the XML test suite into a JUnit test suite designed
 * to drive the POJO through its states and transitions.  It is possible to 
 * set the name of the target package (for the Test Driver class) and the
 * source packages (for the POJO Service-Under-Test class, and any other 
 * user-defined parameters).  We assume that the names of the SUT's methods
 * match the operation names declared in the original service specification.
 * If full testing of visited states and exercised transitions is requested,
 * the SUT must provide two additional methods <em>getState()</em> and
 * <em>getScenario()</em>.  A JavaGrounding is supplied at creation with the
 * PrintWriter to use for streaming the output.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class JavaGrounding extends AbstractGrounding {
	
	/**
	 * Creates a JavaGrounding writing to a stream.  By default, the
	 * target package is the default (unnamed) Java package.
	 * @param stream the output stream.
	 */
	public JavaGrounding(PrintWriter stream) {
		super(stream);
	}
		
	/**
	 * Translates an abstract TestSuite into a grounded suite of tests.  This
	 * is the main entry point to the grounding algorithm, which is usually 
	 * reached by asking a TestSuite to receive this JavaGrounding.
	 * @param testSuite the abstract TestSuite.
	 */
	@Override
	public void groundTestSuite(TestSuite testSuite)  {
		// Calculate package dependencies
		dependency.analyse(testSuite);
		// Create the file preamble, including this package
		writePackageInfo(testSuite);
		writeClassComment(testSuite);
		// JavaFactory must make unchecked type downcasts to user-defined types.
		// This caused problems in JDK 1.6, where warning-suppression in the 
		// Factory#create() method was not enough.  From JDK 1.7, it seems to
		// be OK with that; so this following code is not necessary in JDK 1.7.
		//
		//if (dependency.hasFactory()) {
		//	writer.print("@SuppressWarnings(\"unchecked\")  ");
		//	writer.println("// Downcasting factory.create(...) result");
		//}
		String driver = testSuite.getTestDriver();
		writer.println("public class " + driver + " {");
		
		writeDriverFields(testSuite);
		writeDriverConstructor(testSuite);
		writeSystemSetUp(testSuite);
		
		// The main loop over test sequences
		testIndex = 1;  // first testIndex == 1
		for (TestSequence sequence : testSuite.getTestSequences()) {
			sequence.receive(this);
			++testIndex;  
		}
		writer.println("}");
		writer.println();  // extra blank line for compile-safety
	}

	/**
	 * Translates one abstract TestSequence into a JUnit test-method.  Each
	 * test-method is styled "void testN() {...}", where N is the index of the
	 * TestSequence in the TestSuite.  Each method is prefixed by "@Test" and 
	 * has a body consisting of set-up and evaluation test steps.  Thereafter,
	 * visits each TestStep to generate the method body.
	 */
	@Override
	public void groundTestSequence(TestSequence sequence) {
		writeMethodComment(sequence);
		writer.println("\t@Test");
		writer.print("\tpublic void test" + testIndex + " ()");
		// Test drivers now catch all expected exceptions; do not need this.
		//if (sequence.hasFailures())
		//	writer.print(" throws Exception");
		writer.println(" {");
		// The nested loop over test steps
		stepIndex = 0;  // first stepIndex == 0
		for (TestStep testStep : sequence.getTestSteps()) {
			testStep.receive(this);
			++stepIndex;
		}
		writer.println("\t}");
	}

	/**
	 * Translates one abstract TestStep into Java set-up and evaluation code.
	 * The first TestStep in a TestSequence translates into a creation step, 
	 * which instantiates the SUT.  Subsequent TestSteps translate into method
	 * calls on the SUT's interface.  Methods that return results store these
	 * in variables of the given types, which are named according to the names
	 * of the Outputs, in the style "outputN", where N is the index of the
	 * TestStep in the TestSequence.  If a TestStep is validated, generates
	 * code to compare the actual result(s) with the expected result(s) and
	 * invokes additional methods to query the state of the metadata.
	 */
	@Override
	public void groundTestStep(TestStep testStep) {
		if (testStep.isInitial())
			// Do nothing else
			;
		else if (testStep.isFailure())
			writeFailureTestStep(testStep);
		else 
			writeNormalTestStep(testStep);
		// Check last scenario and state
		if (testStep.isVerify() && metaCheck) {
			writeMetaDataAssertions(testStep);
		}
	}
	
	/**
	 * Prints out package information at the head of the generated JUnit test
	 * driver class source file.  If a target package was set, prints this
	 * out first; then prints the standard JUnit imports; then prints out any
	 * other Java package imports. 
	 * @param testSuite the TestSuite being translated.
	 */
	protected void writePackageInfo(TestSuite testSuite) {
		String driverPackage = dependency.getTargetPackage();
		if (! driverPackage.isEmpty()) {
			writer.println("package " + driverPackage + ";");
			writer.println();
		}
		writer.println("import org.junit.*;");
		writer.println("import static org.junit.Assert.*;");
		writer.println();
		for (String importInfo : dependency.getImports()) {
			writer.println("import " + importInfo + ";");
		}
	}

	/**
	 * Prints out a class-level comment for the JUnit test driver class 
	 * source file.  This includes the name of the test driver and the
	 * date it was created.  Prints out the test conditions under which
	 * the file was generated.
	 * @param testSuite the TestSuite.
	 */
	protected void writeClassComment(TestSuite testSuite) {
		String driver = testSuite.getTestDriver();
		String system = testSuite.getSystem();
		writer.println();
		writer.println("/**");
		writer.println(" * " + driver + " generated on " + new Date());
		writer.print(" * by Broker@Cloud generator");
		writer.println(" uk.ac.sheffield.vtts.ground.JavaGrounding");
		writer.println(" *");
		writer.print(" * System-Under-Test (SUT) is the POJO Java class: ");
		writer.println(system + ",");
		writer.println(" * assumed to be a 'Plain Old Java Object'.");
		writer.println(" *");
		writer.println(" * This is a simple demonstration of grounding to Java, as though the");
		writer.println(" * tested artifact were a simple Java class (POJO) offering methods that");
		writer.println(" * might correspond to the operations of a service.  We assume that the");
		writer.println(" * POJO can be reset simply by creating a fresh instance.  The names of");
		writer.println(" * the POJO's methods must correspond to the original names used in the");
		writer.println(" * specification.  If the POJO offers full inspection of its internal");
		writer.println(" * behaviour, it must offer two further methods, <em>getScenario()</em>");
		writer.println(" * and <em>getState()</em> to report on its status.");
		writer.println(" *");		
		Notice notice = testSuite.getNotice();
		boolean warning = false;
		for (Notice analysis : notice.getNotices()) {
			if (analysis.getClass().getSimpleName().equals("Warning"))
				warning = true;
			else
				writer.println(" *\t\t" + analysis.getText());
		}
		if (warning) 
			writer.println(" *\t\tWarning: specification is not fully covered!");
		writer.println(" */");
	}
	
	/**
	 * Prints out the private attributes used by the JUnit Test Driver class.
	 * These field declarations store the Service-Under-Test and optionally a
	 * JavaFactory, if the TestSuite requires it.
	 * @param testSuite the TestSuite.
	 */
	protected void writeDriverFields(TestSuite testSuite) {
		String system = testSuite.getSystem();
		writer.println();
		writer.println("\t/**");
		writer.println("\t * The Java object representing the System-Under-Test.");
		writer.println("\t */");
		writer.println("\tprivate " + system + " system = null;");
		if (dependency.hasFactory()) {
			writer.println();
			writer.println("\t/**");
			writer.println("\t * JavaFactory to synthesise objects of complex types.");
			writer.println("\t */");
			writer.println("\tprivate JavaFactory factory;");
		}
	}
	
	/**
	 * Prints out the set-up method for creating the System-Under-Test.
	 * This is a @Before method that creates the SUT before each test method.
	 * @param testSuite the TestSuite.
	 */
	protected void writeSystemSetUp(TestSuite testSuite) {
		String system = testSuite.getSystem();
		writer.println();
		writer.println("\t/**");
		writer.println("\t * Creates a fresh instance of the System-Under-Test before each");
		writer.println("\t * test.  Verifies that creation works before the first test.");
		writer.println("\t */");		
		writer.println("\t@Before");
		writer.println("\tpublic void setUp() {");
		writer.println("\t\tif (system == null) {");
		writer.println("\t\t\tsystem = new " + system + "();");
		writer.println("\t\t\tassertNotNull(system);");
		writer.println("\t\t}");
		writer.println("\t\telse");
		writer.println("\t\t\tsystem = new " + system + "();");
		writer.println("\t}");		
	}

	/**
	 * Optionally prints out an assertion check on the result of the set-up
	 * code that created the service.  This should only be done once, if this
	 * TestStep is to be verified.
	 * @param testStep the TestStep.
	 */
	protected void writeInitialTestStep(TestStep testStep) {
		if (testStep.isVerify()) {
			writer.println("\t\t// Verify service reset step #0");
			writer.println("\t\tassertNotNull(system);");	
		}
	}
	
	/**
	 * Prints out a method invocation expecting a normal return value.  If
	 * this TestStep is to be verified, prints out variables to receive the
	 * return value(s) and adds assertion checks to compare expected with
	 * actual results.
	 * @param testStep the TestStep.
	 */
	protected void writeNormalTestStep(TestStep testStep) {
		Operation operation = testStep.getOperation();
		writer.print("\t\t");
		if (testStep.isVerify())
			writeResultVariables(operation.getOutputs());
		writer.print("system." + operation.getName() + "(");
		writeInputList(operation.getInputs());
		writer.println(");");
		if (testStep.isVerify()) {
			writer.println("\t\t// Verify invocation step #" + stepIndex);
			writeResultAssertions(operation.getOutputs());
		}
	}
	
}
