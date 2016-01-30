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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
// import uk.ac.sheffield.vtts.ground.SimpleEntry;  // Uses our public class

/**
 * AbstractFactory is the common ancestor of ModelFactory and JavaFactory.
 * AbstractFactory provides operations for creating instances of basic types
 * and generic List, Set, Map and Pair types.  It provides hooks for creating
 * instances of further uninterpreted types.  Creates instances of all the
 * predefined basic Java types, which have identical names in the modelling
 * language.  For the generic types, maps the type List to ArrayList, Set to
 * HashSet, Map to HashMap and Pair to SimpleEntry.
 * <p>
 * The strongly-typed create() method calls the main createObject() factory
 * method.  This first checks whether the supplied type is generic; if so, 
 * it dispatches to the appropriate method to create a List, Set, Map or Pair
 * object.  Otherwise, it expects to create a simple object.  If the type is
 * not a predefined type, creates an instance of an external type.
 * <p>
 * Normally the value and type are supplied as String values, either using
 * the model language syntax, or Java syntax.  The type String is checked
 * for correct generic syntax.  If the value is null or empty, will create 
 * a default instance of the given type.  For List, Set and Map types, this
 * is an empty instance.  For other types, this is the usual Java default
 * instance (zero, false, etc).  The methods for creating List, Set and
 * Map types expect the value to be the usual printable representation of
 * that collection type; however, if a single element value is provided,
 * a singleton collection will be created.  The wildcard "?" represents the
 * default singleton value.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public abstract class AbstractFactory implements Factory {

	/**
	 * Reports an IllegalArgumentException during object creation.  Raised
	 * if either the type string or value string is badly formed.  This is
	 * a kind of RutimeException.
	 * @param message the error message.
	 * @throws IllegalArgumentException always.
	 */
	protected void error(String message) throws IllegalArgumentException {
		throw new IllegalArgumentException(message);
	}

	/**
	 * Finds the first index of a separator in a String, starting from a
	 * given index, while respecting bracketed structures.  Works like 
	 * indexOf(), except that it skips over bracketed structures that may
	 * also contain the separator character and returns the index of the
	 * first separator found at the top level.  Counts all kinds of brackets,
	 * including angle, square, brace and round brackets.
	 * @param string the String to search.
	 * @param separator the separator character.
	 * @param startIndex the index at which to start searching.
	 * @return the index of the separator, or -1 if it is not found.
	 */
	protected int safeIndexOf(String string, char separator, int startIndex) {
		int brackets = 0;
		for (int index = startIndex; index < string.length(); ++index) {
			char current = string.charAt(index);
			if (current == separator && brackets == 0)
				return index;
			else if ("<[{(".indexOf(current) != -1)
				++brackets;
			else if (">]})".indexOf(current) != -1)
				--brackets;
		}
		return -1;
	}
	
	/**
	 * Safe search routine to split a value-string containing recursively
	 * embedded value-strings.  It works like split(), except that it also
	 * counts the numbers of opening and closing square brackets and braces,
	 * to make sure that the split divides up the topmost values, rather than
	 * embedded values.  The given value-string must be supplied without any 
	 * surrounding brackets or braces.  
	 * @param string the multi-part value, as a String, not bracketed.
	 * @param regex the separator pattern.
	 * @return a list of separated strings.
	 */
	protected List<String> safeSplit(String string, String regex) {
		List<String> result = new ArrayList<String>();
		String search = string + regex;  // guarantees no overflow
		int brackets = 0;
		int start = 0;
		for (int index = 0; index < search.length(); ++index) {
			char current = search.charAt(index);
			if ("<[{(".indexOf(current) != -1)
				++brackets;
			else if (">]})".indexOf(current) != -1)
				--brackets;
			else if (brackets == 0) {
				boolean match = true;
				for (int seek = 0; seek < regex.length(); ++seek) {
					if (regex.charAt(seek) != search.charAt(index + seek)) {
						match = false;
						break;
					}
				}
				if (match) {
					String item = search.substring(start, index);
					start = index + regex.length();
					result.add(item);
				}
			}
		}
		return result;
	}
	
	/**
	 * Try to synthesise an Entity ID in cases where these are manufactured.
	 * The default ID is the first three characters of the Entity type, in
	 * lower case, followed by the digit "1".  This is expected to correspond
	 * to the first legitimate Entity ID in the model.
	 * @param type the type name of the Entity.
	 * @return a synthetic ID string.
	 */
	protected String makeEntityId(String type) {
		String tag = type.substring(0, 4).toLowerCase();
		return tag + "1";
	}

	/**
	 * Creates a strongly-typed instance of any kind of Java Object with the
	 * given printed representation and Java type.  Performs an unchecked 
	 * type cast on the result of createObject().  Provided for convenience.
	 * @param value the value, as a String.
	 * @param type the Java type, as a String.
	 * @return a new instance of the type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(String value, String type) {
		T result = (T) createObject(value, type);
		return result;
	}
	
	/**
	 * Creates a default object of a simple type.  Creates a default instance
	 * of the main types:  String, Integer, Double, Boolean, Character, Long,
	 * Float, Short and Byte.  If the type is not one of these, creates an
	 * instance of a user-defined type.
	 * @param type the type as a String.
	 * @return a new instance of the type.
	 */
	protected Object createDefaultObject(String type) {
		if (type.equals("String"))
			return new String();
		else if (type.equals("Integer"))
			return new Integer(0);
		else if (type.equals("Double"))
			return new Double(0.0);
		else if (type.equals("Boolean"))
			return new Boolean(false);
		else if (type.equals("Character"))
			return new Character('0');
		else if (type.equals("Long"))
			return new Long(0);
		else if (type.equals("Float"))
			return new Float(0.0);
		else if (type.equals("Short"))
			return new Short((short) 0);
		else if (type.equals("Byte"))
			return new Byte((byte) 0);
		else
			return createExternalObject(makeEntityId(type), type);
	}
	
	/**
	 * Creates an object of a simple type, having the given value.  Creates 
	 * an instance of the main simple types:  String, Integer, Double,
	 * Boolean, Character, Long, Float, Short and Byte.  If the type is not
	 * one of these, creates an instance of a user-defined type.
	 * @param value the value, as a String.
	 * @param type the type, as a String.
	 * @return a new instance.
	 */
	protected Object createSimpleObject(String value, String type) {
		if (value == null || value == "")
			return createDefaultObject(type);
		else if (type.equals("String"))
			return value;
		else if (type.equals("Integer"))
			return new Integer(value);
		else if (type.equals("Double"))
			return new Double(value);
		else if (type.equals("Long"))
			return new Long(value);
		else if (type.equals("Boolean"))
			return new Boolean(value);
		else if (type.equals("Character"))
			return new Character(value.charAt(0));
		else if (type.equals("Float"))
			return new Float(value);
		else if (type.equals("Short"))
			return new Short(value);
		else if (type.equals("Byte"))
			return new Byte(value);
		else
			return createExternalObject(value, type);
	}
	
	/**
	 * Creates an instance of an uninterpreted external type, with the given
	 * value.  External types are user-defined types that are uninterpreted
	 * in the modelling language, but could correspond to program types used
	 * by the Service-Under-Test.  This method is abstract and must be
	 * implemented in descendant factories.
	 * @param value an identifier String.
	 * @param type an external type, as a String.
	 * @return a suitable model instance or program instance.
	 */
	protected abstract Object createExternalObject(String value, String type);
	
	/**
	 * Creates a generic List of the given element type, having the given 
	 * value.  The value may be null or empty, in which case an empty List
	 * is returned.  Otherwise, the value must be the printed representation
	 * of a List, surrounded by square brackets, with comma-separated values
	 * as the elements; or a singleton value.
	 * @param value the list-value, as a String.
	 * @param params the generic List type parameters.
	 * @return a new List of the given generic type.
	 */
	protected List<Object> createListObject(String value, 
			List<String> params) {
		if (params.size() != 1)
			error("Badly-formed List type: List" + params);
		List<Object> result = new ArrayList<Object>();
		if (! (value == null || value == "")) {
			int last = value.length() - 1;
			if (value.charAt(0) == '[' &&  value.charAt(last) == ']') {
				String elemList = value.substring(1, last);
				for (String elem : safeSplit(elemList, ", ")) {
					result.add(createObject(elem, params.get(0)));
				}
			}
			else {
				String single = value.equals("?") ? null : value;
				result.add(createObject(single, params.get(0)));
			}
		}
		return result;
	}
	
	/**
	 * Creates a generic Set of the given element type, having the given 
	 * value.  The value may be null or empty, in which case an empty Set
	 * is returned.  Otherwise, the value must be the printed representation
	 * of a Set, surrounded by square brackets, with comma-separated values
	 * as the elements; or a singleton value.
	 * @param value the set-value, as a String.
	 * @param params the generic Set type parameters.
	 * @return a new Set of the given generic type.
	 */
	protected Set<Object> createSetObject(String value, 
			List<String> params) {
		if (params.size() != 1)
			error("Badly-formed Set type: Set" + params);
		Set<Object> result = new HashSet<Object>();
		if (! (value == null || value == "")) {
			int last = value.length() - 1;
			if (value.charAt(0) == '[' &&  value.charAt(last) == ']') {
				String elemList = value.substring(1, last);
				for (String elem : safeSplit(elemList, ", ")) {
					result.add(createObject(elem, params.get(0)));
				}
			}
			else {
				String single = value.equals("?") ? null : value;
				result.add(createObject(single, params.get(0)));
			}
		}
		return result;
	}
	
	/**
	 * Creates a generic Map object of the given parametric type, with the
	 * given value.  The value may be null or empty, in which case an empty
	 * Map is returned.  Otherwise, the value must be the printed 
	 * representation of a Map, surrounded by braces, with comma-separated
	 * values as the pair-elements.  Each pair-element must separate the
	 * first and second values with "=".
	 * @param value the map-value, as a String.
	 * @param params the generic Map type parameters.
	 * @return a new Map of the given generic type.
	 */
	protected Map<Object, Object> createMapObject(String value, 
			List<String> params) {
		if (params.size() != 2)
			error("Badly-formed Map type: Map" + params);
		Map<Object, Object> result = new HashMap<Object, Object>();
		if (! (value == null || value == "")) {
			int last = value.length() - 1;
			if (value.charAt(0) == '{' &&  value.charAt(last) == '}') {
				String elemList = value.substring(1, last);
				for (String elem : safeSplit(elemList, ", ")) {
					Entry<Object, Object> entry = 
							createPairObject(elem, params);
					result.put(entry.getKey(), entry.getValue());
				}
			}
			else {
				String single = value.equals("?") ? null : value;
				Entry<Object, Object> entry =
						createPairObject(single, params);
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	/**
	 * Creates a generic Pair object of the given parametric type, with the
	 * given value.  The value may be null or empty, in which case a default
	 * Entry is created, wrapping two default objects of the first and second
	 * types.  The value may be a key of the first type, in which case an 
	 * Entry is created with that key and a default object of the second type.
	 * Otherwise, the value must be the printed representation of a Pair,
	 * with the first and second components separated by "=".  
	 * @param value the pair-value, as a String.
	 * @param params the generic Entry type parameters.
	 * @return a new Entry of the given generic type.
	 */
	protected Entry<Object, Object> createPairObject(String value, 
			List<String> params) {
		if (params.size() != 2)
			error("Badly-formed Pair type: Entry" + params);
		Entry<Object, Object> result = null;
		if (value == null || value == "") {
			result = new SimpleEntry<Object, Object> (
					createObject(null, params.get(0)),
					createObject(null, params.get(1)));
		}
		else {
			List<String> pair = safeSplit(value, "=");
			if (pair.size() == 1)
				result = new SimpleEntry<Object, Object> (
						createObject(pair.get(0), params.get(0)),
						createObject(null, params.get(1)));
			else if (pair.size() == 2)
			result = new SimpleEntry<Object, Object> (
					createObject(pair.get(0), params.get(0)),
					createObject(pair.get(1), params.get(1)));
			else
				error("Badly-formed Pair value: " + value);
		}
		return result;
	}

}
