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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaFactory is a factory for creating Java objects in the service run-time.
 * JavaFactory is a utility imported by every kind of Java grounding that
 * needs to synthesise Java values of types other than the basic types from 
 * their textual representations.  Whereas the Java compiler may reconstruct
 * values of basic types from their literal representation, it Cannot easily
 * reconstruct instances of List, Set, Map and Pair types from their textual
 * representation.  In this case, the Java grounding algorithm should create
 * a call to this JavaFactory to synthesise the required object, given the
 * Java printed representation of its value, and its Java type.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class JavaFactory extends AbstractFactory {
	
	/**
	 * A list of packages where user-defined classes are located.  This is
	 * the primary source of package information.
	 */
	private List<String> sourcePackages;
	
	/**
	 * A Map from type names to fully-qualified package names.  This is a
	 * cache of where user-defined classes have been discovered.
	 */
	private Map<String, String> classLocations;
	
	/**
	 * Creates a JavaFactory.  Initialises the Map from type names to Java 
	 * package locations for user-defined classes.
	 */
	public JavaFactory() {
		sourcePackages = new ArrayList<String>();
		classLocations = new HashMap<String, String>();
	}
	
	/**
	 * Adds a user-defined source package to the Java package locations known 
	 * by this JavaFactory.  The package may be searched for the existence of
	 * a user-defined type.
	 * @param packageInfo a package location in the Java style.
	 */
	public void useSourcePackage(String packageInfo) {
		sourcePackages.add(packageInfo);
	}
	
	/**
	 * Creates an instance of any kind of Java Object with the given printed
	 * representation and Java type.  This is the principal factory method
	 * for creating object instances.  It expects the type to be supplied in
	 * the Java format (using angle brackets for generic parameters).
	 * @param value the value, as a String.
	 * @param type the Java type, as a String.
	 * @return a new instance of the type.
	 */
	public Object createObject(String value, String type) {
		Object result = null;
		int left = type.indexOf('<');
		int right = type.lastIndexOf('>');
		// Check there exist opening and closing generic brackets
		if (left > -1 && right > -1) {
			String base = type.substring(0, left);
			String params = type.substring(left + 1, right);
			List<String> paramList = safeSplit(params, ", ");
			if (base.equals("ArrayList"))
				result = createListObject(value, paramList);
			else if (base.equals("HashSet"))
				result = createSetObject(value, paramList);
			else if (base.equals("HashMap"))
				result = createMapObject(value, paramList);
			else if (base.equals("SimpleEntry"))
				result = createPairObject(value, paramList);
			else
				error("Unrecognised generic type: " + type);
		}
		// Check there are no opening and closing generic brackets
		else if (left + right == -2)
			result = createSimpleObject(value, type);
		else
			error("Badly-formed generic type: " + type);
		return result;
	}
	
	/**
	 * Finds the Java class definition for an external user-defined type.  If
	 * the class location is already known, returns the Java class; otherwise
	 * attempts to find the class in each of the supplied user packages, and
	 * eventually the default package.
	 * @param type the simple name of the Java class type.
	 * @return the Java class definition of the type.
	 * @throws ClassNotFoundException if no class definition is found.
	 */
	protected Class<?> findClassDefinition(String type) 
			throws ClassNotFoundException {
		if (! classLocations.containsKey(type)) {
			for (String userPackage : sourcePackages) {
				String packageInfo = userPackage.isEmpty() ? type :
					userPackage + '.' + type;
				try {
					Class<?> result = Class.forName(packageInfo);
					classLocations.put(type, packageInfo);
					return result;  // exit as soon as found
				}
				catch (ClassNotFoundException ex) {
				}
			}
			classLocations.put(type, type);  // try default package
		}
		return Class.forName(classLocations.get(type));
	}

	/**
	 * Creates an instance of an uninterpreted external type, with the given
	 * value.  External types are uninterpreted user-defined types used by 
	 * the Service-Under-Test.  Provided the Java package location of this 
	 * type was previously added to this JavaFactory, creates an instance of
	 * the user-defined type, expecting it to have a constructor that accepts
	 * a single String argument as some kind of identifier.
	 * @param value the identifier for this object, as a String.
	 * @param type the Java type of this object, as a String.
	 * @return the created instance.
	 */
	protected Object createExternalObject(String value, String type) {
		Object result = null;
		try {
			Class<?> classType = findClassDefinition(type);
			Constructor<?> create = classType.getConstructor(String.class);
			result = create.newInstance(value);
		} 
		catch (ClassNotFoundException e) {
			error("Cannot find the external Java class: " + 
					classLocations.get(type));
		} 
		catch (NoSuchMethodException e) {
			error("External class has no String constructor: " + 
					classLocations.get(type));
		}
		catch (Exception e) {
			error("External class String constructor failed: " + 
					classLocations.get(type));
		}
		return result;
	}

}
