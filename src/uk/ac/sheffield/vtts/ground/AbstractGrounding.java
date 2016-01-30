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
import java.util.Set;

import uk.ac.sheffield.vtts.model.Failure;
import uk.ac.sheffield.vtts.model.Input;
import uk.ac.sheffield.vtts.model.Operation;
import uk.ac.sheffield.vtts.model.Output;
import uk.ac.sheffield.vtts.model.Parameter;
import uk.ac.sheffield.vtts.model.TestSequence;
import uk.ac.sheffield.vtts.model.TestStep;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * AbstractGrounding is the common ancestor of JUnit-based code generators.
 * AbstractGrounding is the ancestor of JavaGrounding, JaxWsGrounding, 
 * JaxRsGrounding and other JUnit-based code generators.  It provides the
 * default implementation of many of the Java code-generation routines that
 * are shared by subclasses.  It is possible to set the name of the target
 * package (for the generated Test Driver class) and the names of one or more
 * source packages (for the service resources, including the class denoting
 * the Service-Under-Test).  It is possible to request full checking of all
 * visited states and exercised transitions (meta-data used during test-mode)
 * so long as the implemented service offers the additional inspection 
 * methods <em>getState()</em> and <em>getScenario()</em>.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public abstract class AbstractGrounding implements Grounding {
	
	/**
	 * Records package dependencies submitted by the end-user, so that the
	 * standard JavaFactory can create instances of these types.
	 */
	protected JavaDependency dependency;
	
	/**
	 * The output stream to which this Grounding writes generated code.
	 * This is supplied at creation of this AbstractGrounding.  
	 */
	protected PrintWriter writer;
	
	/**
	 * Boolean flag indicating whether to check full meta-information for
	 * triggered operation scenarios and reached states.
	 */
	protected boolean metaCheck;
	
	/**
	 * The counter for TestSequences.  Starts with 1, for the first test.
	 */
	protected int testIndex;
	
	/**
	 * The counter for TestSteps.  Starts with step 0, for system creation.
	 */
	protected int stepIndex;
	
	/**
	 * Creates an AbstractGrounding writing to a stream.  By default, the
	 * target package is the default (unnamed) Java package.
	 * @param stream the output stream.
	 */
	protected AbstractGrounding(PrintWriter stream) {
		dependency = new JavaDependency();
		writer = stream;
		metaCheck = true;  // Default setting
	}
		
	/**
	 * Sets the name of the target Java package for the JUnit Test Driver.
	 * @param packageInfo the Test Driver package name.
	 */
	@Override
	public void useTargetPackage(String packageInfo) {
		dependency.useTargetPackage(packageInfo);
	}
	
	/**
	 * Sets the name of the source Java package for the System-Under-Test.
	 * @param packageInfo the Service package name.
	 */
	@Override
	public void useSourcePackage(String packageInfo) {
		dependency.useSourcePackage(packageInfo);
	}

	/**
	 * Tells this Grounding to use the given root URI as the endpoint for the
	 * service (optional).  This is a null operation, overridden in subclasses
	 * that need to accept an endpoint.
	 * @param serviceURI the root URI of the service.
	 */
	@Override
	public void useEndpoint(String serviceURI) {
	}

	/**
	 * Sets whether to generate full checks for service meta-data, such as 
	 * the name of the last triggered scenario, or the last reached state.
	 * @param value true, to enable full checks of service meta-data; false
	 * to disable such checks for services that do not reveal this meta-data.
	 */
	public void setMetaCheck(boolean value) {
		metaCheck = value;
	}
	
	/**
	 * Prints out the test driver's constructor, with or without the Factory.
	 * If the JUnit test driver class needs the JavaFactory, adds extra
	 * creation and initialisation instructions in the constructor.
	 * @param testSuite the TestSuite.
	 */
	protected void writeDriverConstructor(TestSuite testSuite) {
		String driver = testSuite.getTestDriver();
		writer.println();
		writer.println("\t/**");
		writer.println("\t * Creates the JUnit test driver: " + driver + ".");
		writer.println("\t */");
			writer.println("\tpublic " + driver + "() {");
		if (dependency.hasFactory()) {
			// The constructor to create the Factory object.
			writer.println("\t\tfactory = new JavaFactory();");
			for(String packageInfo : dependency.getSourcePackages()) {
				writer.println("\t\tfactory.useSourcePackage(\"" 
						+ packageInfo + "\");");
			}
		}
		writer.println("\t}");			
	}

	/**
	 * Prints out a method-level comment for one test-method of the JUnit
	 * driver class source file.  
	 * @param sequence the TestSequence
	 */
	protected void writeMethodComment(TestSequence sequence) {
		int testIndex = sequence.getTest();
		String state = sequence.getState();
		int path = sequence.getPath();
		int extra = -1;  // The number of extra merged tests
		for (TestStep testStep : sequence.getTestSteps()) {
			if (testStep.isVerify())
				++extra;  // Count up all verification steps
		}
		writer.println();
		writer.println("\t/**");
		writer.print("\t * Translation of TestSequence #" + testIndex);
		writer.println(".  The main test goal is to reach the ");
		writer.print("\t * state '" + state + "' and, from there, execute a novel");
		writer.println(" path of length " + path + ".");
		if (extra > 0) {
			writer.print("\t * This test also contains a further ");
			writer.println(extra == 1 ? "merged shorter test." : 
				(extra + " merged shorter tests."));
		}
		writer.println("\t */");
	}

	/**
	 * Prints out a method invocation expecting to throw an exception.  If
	 * this TestStep is to be verified, inserts an automatic JUnit fail()
	 * assertion after the tested method, which causes this test to fail,
	 * if the method was executed without throwing an exception.
	 * @param testStep the TestStep.
	 */
	protected void writeFailureTestStep(TestStep testStep) {
		Operation operation = testStep.getOperation();
		Failure fail = operation.getFailures().iterator().next();
		writer.println("\t\ttry {");
		writer.print("\t\t\tsystem." + operation.getName() + "(");
		writeInputList(operation.getInputs());
		writer.println(");");
		if (testStep.isVerify()) {
			writer.println("\t\t\t// Verify exception step #" + stepIndex);
			writer.println("\t\t\tfail(\"Expected an exception: " 
					+ fail.getContent() + "\");");
		}
		writer.println("\t\t}");
		writer.println("\t\tcatch (Exception ex) {");
		if (testStep.isVerify()) 
			writer.println("\t\t\tassertTrue(ex.getMessage().contains(\"" 
					+ fail.getContent() + "\"));");
		else
			writer.println("\t\t\t// Exception has already been verified.");
		writer.println("\t\t}");
	}

	/**
	 * Prints the appropriate kind of receiver variable(s) for the outputs.
	 * @param outputs the ordered set of outputs.
	 */
	protected void writeResultVariables(Set<Output> outputs) {
		int outCount = outputs.size();
		if (outCount > 1) {
			writer.print("Object[] actual" + stepIndex + " = ");
		}
		else if (outCount == 1) {
			Output out = outputs.iterator().next();
			String outName = out.getName();
			writeJavaType(out);
			writer.print(" " + outName + stepIndex + " = ");
		}
	}

	/**
	 * Prints the list of argument value(s) for the operation inputs.
	 * @param inputs the ordered set of inputs.
	 */
	protected void writeInputList(Set<Input> inputs) {
		int inCount = 0;
		for(Input in : inputs) {
			if (inCount++ > 0)
				writer.print(", ");
			writeJavaValue(in);
		}
	}

	/**
	 * Prints the initialisation list of literalTypes values for complex outputs.
	 * @param outputs the ordered set of outputs.
	 */
	protected void writeOutputList(Set<Output> outputs) {
		int outCount = 0;
		for(Output out : outputs) {
			if (outCount++ > 0)
				writer.print(", ");
			writeJavaValue(out);
		}
	}

	/**
	 * Prints the type name of a Parameter as a Java type.
	 * @param parameter any Input or Output parameter.
	 */
	protected void writeJavaType(Parameter parameter) {
		// Converts Broker@Cloud type to Java type
		String type = parameter.getType();
		if (type.indexOf('[') + type.indexOf(']') >= 0) {
			type = type.replace('[', '<').replace(']', '>');
		}
		if (type.contains("List"))
			type = type.replace("List", "ArrayList");
		if (type.contains("Set"))
			type = type.replace("Set", "HashSet");
		if (type.contains("Map"))
			type = type.replace("Map", "HashMap");
		if (type.contains("Pair"))
			type = type.replace("Pair", "SimpleEntry");
		writer.print(type);
	}

	/**
	 * Grounds the value of an Input or Output parameter.  If the value is of
	 * some basic or literal Java type, prints out its literal value; 
	 * otherwise prints out a factory expression to create the value.
	 * @param parameter any Input or Output parameter.
	 */
	protected void writeJavaValue(Parameter parameter) {
		final String literalTypes = "String, Integer, Double, Boolean, " +
				"Character, Short, Float, Long, Byte";
		if (! parameter.isBound())  // Safety check for binding.
			writer.print("null");
		else if(literalTypes.contains(parameter.getType()))
			writeLiteralValue(parameter);
		else
			writeFactoryValue(parameter);
	}

	/**
	 * Prints out the literal value of a simple Input or Output parameter.
	 * @param parameter any simple Input or Output parameter.
	 */
	protected void writeLiteralValue(Parameter parameter) {
		String type = parameter.getType();
		// Use parameter.evaluate() to achieve default initialisation.
		if (type.equals("String"))
			writer.print("\"" + parameter.evaluate() + "\"");
		else if (type.equals("Character"))
			writer.print("\'" + parameter.evaluate() + "\'");
		else if (type.equals("Float"))
			writer.print(parameter.evaluate() + "F");
		else if (type.equals("Long"))
			writer.print(parameter.evaluate() + "L");
		else
			writer.print(parameter.evaluate());
	}

	/**
	 * Prints out a factory expression to create the value of a complex
	 * Input or Output parameter.
	 * @param parameter any complex Input or Output parameter.
	 */
	protected void writeFactoryValue(Parameter parameter) {
		String value = parameter.getContent();
		writer.print("(");
		writeJavaType(parameter);
		writer.println(")");
		writer.print("\t\t\tfactory.create(\"");
		writer.print(value);
		writer.print("\", \"");
		writeJavaType(parameter);
		writer.print("\")");
	}

	/**
	 * Prints the JUnit assertion(s) checking that the expected and actual
	 * result(s) corresponded.  In some groundings, we can also check that
	 * a void result was returned for an operation with no result specified.
	 * @param outputs the expected set of outputs.
	 */
	protected void writeResultAssertions(Set<Output> outputs) {
		int outCount = outputs.size();
		if (outCount == 0)
			writeVoidAssertion();
		else if (outCount == 1) {
			Output out = outputs.iterator().next();
			String outName = out.getName();
			writer.print("\t\tassertEquals((");
			writeJavaType(out);
			writer.print(") ");
			writeJavaValue(out);  // expected
			writer.print(", " + outName + stepIndex);
			writer.println(");");
		}
		else {  // outCount > 1
			writer.print("\t\tObject[] expect" + stepIndex + " = {");
			writeOutputList(outputs);
			writer.println("};");
			writer.print("\t\tassertArrayEquals(expect" + stepIndex);
			writer.println(", actual" + stepIndex + ");");
		}
	}
	
	/**
	 * Placeholder method for subclasses to override if they must check that
	 * the result from a call was void.  Services should not reveal any
	 * information.  But the POJO grounding cannot do anything here.
	 */
	protected void writeVoidAssertion() {
	}

	/**
	 * Prints the JUnit assertions checking that the system executed the
	 * expected scenario and ended in the expected state.
	 * @param testStep the TestStep to be asserted.
	 */
	protected void writeMetaDataAssertions(TestStep testStep) {
		String state = testStep.getState();
		String branch = testStep.getName();
		int index = branch.indexOf('[');
		if (index != -1)
			branch = branch.substring(0, index);
		writer.print("\t\tassertEquals(\"");
		writer.println(branch + "\", system.getScenario());");
		writer.print("\t\tassertEquals(\"");
		writer.println(state + "\", system.getState());");		
	}
	
}
