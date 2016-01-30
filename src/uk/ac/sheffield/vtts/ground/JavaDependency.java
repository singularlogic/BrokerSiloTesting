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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.sheffield.vtts.model.Input;
import uk.ac.sheffield.vtts.model.Operation;
import uk.ac.sheffield.vtts.model.Output;
import uk.ac.sheffield.vtts.model.TestSequence;
import uk.ac.sheffield.vtts.model.TestStep;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * JavaDependency analyses the dependency of test code on external packages.
 * JavaDependency has the task of helping a Java-based grounding to resolve
 * its dependencies on external Java packages.  It maintains the relationship
 * between the target package (for the generated Test Driver) and any source
 * packages (for external user-defined classes), so that the correct import
 * statements are included in the generated code; and also ensures that, if a
 * JavaFactory is required to synthesise instances of external types, it is
 * notified about the external user packages in which to search.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class JavaDependency {
	
	/**
	 * The assumed package location of the JavaFactory factory, which is used
	 * to create instances of complex (generic) types, or user-defined types.
	 */
	public static String factoryLocation = "uk.ac.sheffield.vtts.ground.JavaFactory";
	
	/**
	 * The Set of simple type names.  This is used to filter all input and 
	 * output types.  If other types are encountered, then a JavaFactory will
	 * be needed.
	 */
	private final Set<String> simpleTypes;
	
	/**
	 * A Map from simple type names to package-qualified type names, for Java
	 * system utility imports.
	 */
	private Map<String, String> javaLocations;
	
	/**
	 * The package location of the generated target file for the test driver.
	 */
	private String targetPackage = "";  // the default package
	
	/**
	 * A Set of user supplied package locations where classes are defined.
	 */
	private Set<String> sourcePackages;
	
	/**
	 * Flag indicating whether the analysed service has generic inputs.
	 */
	private boolean genericInputs = false;
	
	/**
	 * Flag indicating whether the analysed service has generic outputs.
	 */
	private boolean genericOutputs = false;
	
	/**
	 * Flag indicating whether the analysed service has factory inputs.
	 */
	private boolean factoryInputs = false;
	
	/**
	 * Flag indicating whether the analysed service has factory outputs.
	 */
	private boolean factoryOutputs = false;
	
	/**
	 * Constructs this JavaDependency visitor.  Initialises the package map
	 * for Java class dependencies, and the list of user packages.
	 */
	public JavaDependency() {
		simpleTypes = new HashSet<String>(
				Arrays.asList("String", "Integer", "Double", "Long", 
						"Boolean", "Character", "Float", "Short", "Byte"));
		javaLocations = new LinkedHashMap<String, String>();
		sourcePackages = new LinkedHashSet<String>();
	}
	
	/**
	 * Analyses the TestSuite looking for complex types in the inputs and
	 * outputs of each tested Operation.  Complex types include generic types
	 * and user-defined class types.  These may require specific package 
	 * import statements, or a JavaFactory to synthesise values, or both.
	 * Iterates over every TestStep in every TestSequence in the TestSuite
	 * and analyses the type of every Input and Output of each Operation.
	 * @param testSuite the TestSuite to be analysed.
	 */
	public void analyse(TestSuite testSuite) {
		for (TestSequence sequence : testSuite.getTestSequences()) {
			for (TestStep testStep : sequence.getTestSteps()) {
				Operation operation = testStep.getOperation();
				// Analyse the Operation's inputs
				for (Input input : operation.getInputs())
					analyseInput(input);
				// Analyse the Operation's outputs
				for (Output output : operation.getOutputs())
					analyseOutput(output);
			}
		}
	}
	
	/**
	 * Analyses one Input parameter to determine whether it is of a complex
	 * generic, or factory-created type.
	 * @param input the Input parameter.
	 */
	protected void analyseInput(Input input) {
		String type = input.getType();
		if (! simpleTypes.contains(type)) {
			factoryInputs = true;
			if (isGeneric(type))
				genericInputs = true;
			// We will need a factory to synthesise such objects
			javaLocations.put("Factory", factoryLocation);
			// Store the required mappings
			if (type.startsWith("List"))
				javaLocations.put("List", "java.util.ArrayList");
			else if (type.startsWith("Set"))
				javaLocations.put("Set", "java.util.HashSet");
			else if (type.startsWith("Map"))
				javaLocations.put("Map", "java.util.HashMap");
			else if (type.startsWith("Pair"))
				javaLocations.put("Pair", "uk.ac.sheffield.vtts.ground.SimpleEntry");
		}
	}
	
	/**
	 * Analyses one Output parameter to determine whether it is of a complex
	 * generic, or factory-created type.
	 * @param output the Output parameter.
	 */
	protected void analyseOutput(Output output) {
		String type = output.getType();
		if (! simpleTypes.contains(type)) {
			factoryOutputs = true;
			if (isGeneric(type))
				genericOutputs = true;
			// We will need a factory to synthesise such objects
			javaLocations.put("Factory", factoryLocation);
			// Store the required mappings
			if (type.startsWith("List"))
				javaLocations.put("List", "java.util.ArrayList");
			else if (type.startsWith("Set"))
				javaLocations.put("Set", "java.util.HashSet");
			else if (type.startsWith("Map"))
				javaLocations.put("Map", "java.util.HashMap");
			else if (type.startsWith("Pair"))
				javaLocations.put("Pair", "uk.ac.sheffield.vtts.ground.SimpleEntry");
		}
	}

	/**
	 * Tests whether the supplied model type is a generic type.
	 * @param type the model type, possibly containing [] generic brackets.
	 * @return true, if the model type contains [] generic brackets.
	 */
	protected boolean isGeneric(String type) {
		return type.indexOf('[') + type.indexOf(']') > 0;
	}

	/**
	 * Adds a package path name to the set of package names for source files.
	 * Both of the null package name and the empty String are treated as the 
	 * default package name.
	 * @param packageInfo the package location as a String.
	 */
	public void useSourcePackage(String packageInfo) {
		sourcePackages.add(packageInfo == null ? "" : packageInfo);
	}
	
	/**
	 * Sets the path name of the package for the generated target file.  
	 * Both of the null package name and the empty String are treated as the 
	 * default package name.
	 * @param packageInfo the package location as a String.
	 */
	public void useTargetPackage(String packageInfo) {
		targetPackage = (packageInfo == null ? "" : packageInfo);
	}

	/**
	 * Returns the user-defined source package names as a set of Strings.
	 * @return a set of user-defined source packages.
	 */
	public Set<String> getSourcePackages() {
		return sourcePackages;
	}
	
	/**
	 * Returns the name of the user-defined target package.  If not set, this
	 * is the empty String representing the default package.
	 * @return the name of the target package.
	 */
	public String getTargetPackage() {
		return targetPackage;
	}
	
	/**
	 * Reports whether this JavaDependency detected the need for a Factory.
	 * If the tests contained inputs or outputs of complex types, that is,
	 * any type other than the simple types, a JavaFactory will be needed.
	 * Tests whether the "Factory" key was added to the Java imports, after
	 * analysing the TestSuite.
	 * @return true, if a JavaFactory is needed.
	 */
	public boolean hasFactory() {
		return javaLocations.containsKey("Factory");
	}
	
	/**
	 * Reports whether this JavaDependency has detected a need to encode
	 * generic inputs.  A suitable encoder into JSON may be required.
	 * @return true if tests accept generic inputs.
	 */
	public boolean hasGenericInputs() {
		return genericInputs;
	}
	
	/**
	 * Reports whether this JavaDependency has detected a need to decode
	 * generic outputs.  A suitable decoder from JSON may be required.
	 * @return true if tests return generic outputs.
	 */
	public boolean hasGenericOutputs() {
		return genericOutputs;
	}
	
	/**
	 * Reports whether this JavaDependency has detected a need to build
	 * factory inputs.  This will use the JavaFactory.
	 * @return true, if tests accept factory-generated inputs.
	 */
	public boolean hasFactoryInputs() {
		return factoryInputs;
	}
	
	/**
	 * Reports whether this JavaDependency has detected a need to build
	 * factory outputs.  This will use the JavaFactory.
	 * @return true, if tests return factory-generated outputs.
	 */
	public boolean hasFactoryOutputs() {
		return factoryOutputs;
	}

	/**
	 * Returns all the needed package locations as a List of Strings.  This is
	 * a list of individual Java class imports and whole user-package imports.
	 * The early imports in this list are utility Java classes; and the later
	 * imports are user-declared source packages (minus the target package).
	 * @return a list of Java and user package locations.
	 */
	public List<String> getImports() {
		List<String> result = new ArrayList<String>();
		result.addAll(javaLocations.values());
		for (String packageInfo : sourcePackages) {
			if (! packageInfo.equals(targetPackage))
				result.add(packageInfo + ".*");
		}
		return result;
	}

}
