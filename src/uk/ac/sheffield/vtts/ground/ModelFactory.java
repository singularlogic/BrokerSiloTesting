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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.sheffield.vtts.model.Entity;

/**
 * ModelFactory is a factory for creating Java models for model-simulation 
 * purposes.  ModelFactory is used during model-checking and simulation, to
 * create simple Java models that can be evaluated and compared by the
 * model-checking tools.  It expects values and types to be supplied in the
 * syntax of the model expression language.  As well as being able to create
 * instances of simple and generic types, ModelFactory is able to synthesise
 * the successor and predecessor objects for existing objects of these types.
 * This capability is useful when exploring around the edges of a model
 * specification.  A single static ModelFactory is created in Expression and
 * shared by all of its subclasses.
 * <p>
 * ModelFactory also offers an API for extracting parts of model types,
 * expressed as possibly nested types in the expression language.  As well
 * as extracting the value-type of lists and the key- and value-types of
 * maps, it is possible to extract the cognate pair-type for map entries.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class ModelFactory extends AbstractFactory {
	
	/**
	 * Creates a default instance of any kind of model Object with the given
	 * model type.  This is a convenience for creating model instances when
	 * their precise value is not known.  It expects the type to be supplied
	 * in the modelling language format (using square brackets for generic 
	 * parameters).
	 * @param type the model type, as a String.
	 * @return the default instance of the model type.
	 */
	public Object createObject(String type) {
		return createObject(null, type);
	}

	/**
	 * Creates an instance of any kind of model Object with the given printed
	 * representation and model type.  This is the principal factory method
	 * for creating model instances.  It expects the type to be supplied in
	 * the modelling language format (using square brackets for generic 
	 * parameters).
	 * @param value the value, as a String.
	 * @param type the model type, as a String.
	 * @return a new instance of the model type.
	 */
	public Object createObject(String value, String type) {
		Object result = null;
		int left = type.indexOf('[');
		int right = type.lastIndexOf(']');
		if (left > -1 && right > -1) {
			String base = type.substring(0, left);
			String params = type.substring(left + 1, right);
			List<String> paramList = safeSplit(params, ", ");
			if (base.equals("List"))
				result = createListObject(value, paramList);
			else if (base.equals("Set"))
				result = createSetObject(value, paramList);
			else if (base.equals("Map"))
				result = createMapObject(value, paramList);
			else if (base.equals("Pair"))
				result = createPairObject(value, paramList);
			else
				error("unrecognised generic type: " + type);
		}
		else if (left + right == -2)
			result = createSimpleObject(value, type);
		else
			error("Badly-formed generic type: " + type);
		return result;
	}

	/**
	 * Creates an instance of an uninterpreted external type, with the given
	 * value.  External types are user-defined types that are uninterpreted
	 * in the modelling language.  This ModelFactory creates an instance of
	 * the model type Entity, which is used to model all uninterpreted types.
	 * Entities may be compared for Equality and are hashed on their String
	 * identifier, but support no other operations.
	 * @param value an identifier String.
	 * @param type an external type, as a String.
	 * @return an instance of the Entity model.
	 */
	@Override
	protected Object createExternalObject(String value, String type) {
		if (value == null)
			error("Uninterpreted type has no identifier: " + type);
		return new Entity(value);
	}

	/**
	 * Extracts the base-type name from any parametric type.
	 * The supplied paramType is the parametric type of a List, Set, Map or
	 * Pair.  Searches for the base-type name, which is the first symbol 
	 * found before the first opening square bracket '['.
	 * @param paramType the parametric type name.
	 * @return the base-type, one of List, Set, Map or Pair.
	 */
	public String getBaseType(String paramType) {
		int left = paramType.indexOf('[');
		int right = paramType.lastIndexOf(']');  // just checking this
		if (left == -1 || right == -1)
			error("Cannot extract base type of: " + paramType);
		return paramType.substring(0, left);
	}

	/**
	 * Extracts the value-type name from any parametric type.
	 * The supplied paramType is the parametric type of a List, Set, Map or 
	 * Pair.  Searches for the value-type name, which is the last symbol 
	 * found before the last closing square bracket ']'.  If paramType is 
	 * either a map-type or pair-type with two actual type parameters, these
	 * must be separated by ", ".
	 * @param paramType the parametric type name.
	 * @return the value-type, the main element type, or second projection.
	 */
	public String getValueType(String paramType) {
		int left = paramType.indexOf('[');  // just checking this
		int right = paramType.lastIndexOf(']');
		if (left == -1 || right == -1)
			error("Cannot extract value type of: " + paramType);
		int space = safeIndexOf(paramType, ' ', left+1);
		int start = (space < 0 ? left : space) + 1;
		return paramType.substring(start, right);
	}

	/**
	 * Extracts the search key-type name from any parametric type.
	 * The supplied paramType is the parametric type of a List, Set, Map or
	 * Pair.  If paramType is a map-type or pair-type with two actual type
	 * parameters separated by ", ", searches for the key-type name, which is
	 * the first symbol found after the first opening square bracket '['.  If
	 * the paramType is a list-type or set-type, returns "Integer" as the
	 * index type.
	 * @param paramType the parametric type name.
	 * @return the search key-type, the index type or first projection.
	 */
	public String getKeyType(String paramType) {
		int left = paramType.indexOf('[');
		int right = paramType.lastIndexOf(']');  // just checking this
		int end = safeIndexOf(paramType, ',', left+1);
		if (left == -1 || right == -1)
			error("Cannot extract key type of: " + paramType);
		if (end != -1)
			return paramType.substring(left + 1, end);
		else
			return "Integer";
	}

	/**
	 * Extracts the tuple pair-type name from any parametric type.
	 * The supplied paramType is the parametric type of a List, Set, Map or
	 * Pair.  If paramType is a map-type "Map[K, V]" or pair-type "Pair[K, 
	 * V]", returns the related pair-type: "Pair[K, V]".  If paramType is a
	 * list-type "List[T]" or set-type "Set[T]", returns the synthetic 
	 * pair-type: "Pair[Integer, T]".
	 * @param paramType the parametric type name.
	 * @return the pair-type for keyed (or indexed) elements.
	 */
	public String getPairType(String paramType) {
		int left = paramType.indexOf('[');
		int right = paramType.lastIndexOf(']');
		int end = safeIndexOf(paramType, ',', left+1);
		if (left == -1 || right == -1)
			error("Cannot extract pair type of: " + paramType);
		if (end != -1)
			return "Pair[" + paramType.substring(left + 1);
		else
			return "Pair[Integer, " + paramType.substring(left + 1);
	}
	
	/**
	 * Returns the successor of a value of the given type.  Creates the next
	 * monotonically greater object.  Integral values grow by unit increment,
	 * floating point values grow by a decimal fraction and strings acquire 
	 * an alphabetically-posterior suffix.  Collections increase in size by 
	 * one element and Pairs increment their wrapped projections.
	 * Other external uninterpreted types are returned unchanged.
	 * @param value a non-null value.
	 * @param type the model type.
	 * @return the next greater value.
	 */
	public Object getSuccessor(Object value, String type) {
		if (type.equals("String"))
			return ((String) value) + "~Z";  // later than value
		else if (type.equals("Integer"))
			return ((Integer) value) + 1;
		else if (type.equals("Double"))
			return ((Double) value) + 0.3;
		else if (type.equals("Long"))
			return ((Long) value) + 1;
		else if (type.equals("Boolean")) 
			return ! (Boolean) value;
		else if (type.equals("Character"))
			return (Character) ((Character) value) + 1;
		else if (type.equals("Float"))
			return ((Float) value) + 0.3;
		else if (type.equals("Short"))
			return ((Short) value) + 1;
		else if (type.equals("Byte"))
			return ((Byte) value) + 1;
		else if (type.startsWith("List"))
			return getNextList((List<?>) value, type, true);
		else if (type.startsWith("Set"))
			return getNextSet((Set<?>) value, type, true);
		else if (type.startsWith("Map"))
			return getNextMap((Map<?, ?>) value, type, true);
		else if (type.startsWith("Pair"))
			return getNextPair((Entry<?, ?>) value, type, true);
		else 
			return value;  // unchanged -- a failsafe for other types
	}

	/**
	 * Returns the predecessor of a value of the given type.  Creates the last
	 * monotonically lesser object.  Integral values shrink by a unit decrement,
	 * floating point values shrink by a decimal fraction and strings acquire 
	 * an alphabetically-prior prefix.  Collections, if not already empty, 
	 * reduce in size by one element, and Pairs decrement their wrapped
	 * projections.  Bottom values and other uninterpreted external types
	 * remain unchanged.
	 * @param value a non-null value.
	 * @param type the model type.
	 * @return the previous smaller value.
	 */
	public Object getPredecessor(Object value, String type) {
		if (type.equals("String"))
			return (value == "" ? value : 
				"A-" + (String) value);  // earlier than value
		else if (type.equals("Integer"))
			return ((Integer) value) - 1;
		else if (type.equals("Double"))
			return ((Double) value) - 0.3;
		else if (type.equals("Long"))
			return ((Long) value) - 1;
		else if (type.equals("Boolean")) 
			return ! (Boolean) value;
		else if (type.equals("Character"))
			return (value == (Character) '0' ? value : 
				(Character) ((Character) value) - 1);
		else if (type.equals("Float"))
			return ((Float) value) - 0.3;
		else if (type.equals("Short"))
			return ((Short) value) - 1;
		else if (type.equals("Byte"))
			return ((Byte) value) - 1;
		else if (type.startsWith("List"))
			return getNextList((List<?>) value, type, false);
		else if (type.startsWith("Set"))
			return getNextSet((Set<?>) value, type, false);
		else if (type.startsWith("Map"))
			return getNextMap((Map<?, ?>) value, type, false);
		else if (type.startsWith("Pair"))
			return getNextPair((Entry<?, ?>) value, type, false);
		else 
			return value;  // unchanged -- a failsafe for other types
	}

	/**
	 * Creates the successor, or predecessor of a list, depending on a flag.
	 * If the flag is true, creates the successor list containing one more
	 * element, which is the successor element of the last current element
	 * (if any).  Otherwise, removes the last element of the list (if any).
	 * @param list the original list.
	 * @param type the parametric type of the list.
	 * @param more indicates the successor (true) or predecessor (false).
	 * @return the successor or predecessor of the list, if any.
	 */
	protected Object getNextList(List<?> list, String type, boolean more) {
		List<Object> result = new ArrayList<Object>(list);
		int lastIndex = list.size() - 1;
		if (more) {
			String valueType = getValueType(type);
			if (list.isEmpty()) 
				result.add(createObject(null, valueType));
			else
				result.add(getSuccessor(list.get(lastIndex), valueType));
		}
		else if (! list.isEmpty())
			result.remove(lastIndex);
		return result;
	}

	/**
	 * Creates the successor, or predecessor of a set, depending on a flag.
	 * If the flag is true, creates the successor set containing one more
	 * element, which is the first successor of a random element that is not
	 * already in the set.  Otherwise, removes a random element (if any).
	 * @param set the original set.
	 * @param type the parametric type of the set.
	 * @param more indicates the successor (true) or predecessor (false).
	 * @return the successor or predecessor of the set, if any.
	 */
	protected Object getNextSet(Set<?> set, String type, boolean more) {
		Set<Object> result = new HashSet<Object>(set);
		Object elem = set.isEmpty() ? null : set.iterator().next();
		if (more) {
			String valueType = getValueType(type);
			if (elem == null)
				elem = createObject(null, valueType);
			while (result.contains(elem)) {
				elem = getSuccessor(elem, valueType);
			}
			result.add(elem);
		}
		else if (! set.isEmpty())
			result.remove(elem);
		return result;
	}
	
	/**
	 * Creates the successor, or predecessor of a map, depending on a flag.
	 * If the flag is true, creates the successor map containing one more
	 * pair, which is the first successor of a random pair whose key is not
	 * already in the map.  Otherwise, removes a random pair (if any).
	 * @param map the original map.
	 * @param type the parametric type of the map.
	 * @param more indicates the successor (true) or predecessor (false).
	 * @return the successor or predecessor of the map, if any.
	 */
	protected Object getNextMap(Map<?, ?> map, String type, boolean more) {
		Map<Object, Object> result = new HashMap<Object, Object>(map);
		Object key = map.isEmpty() ? null : 
			map.keySet().iterator().next();
		if (more) {
			String keyType = getKeyType(type);
			String valueType = getValueType(type);
			if (key == null)
				key = createObject(null, keyType);
			Object value = map.get(key);
			if (value == null)
				value = createObject(null, valueType);
			while (result.containsKey(key)) {
				key = getSuccessor(key, keyType);
				value = getSuccessor(value, valueType);
			}
			result.put(key, value);			
		}
		else if (! map.isEmpty())
			result.remove(key);
		return result;
	}


	/**
	 * Creates the successor, or predecessor of a pair, depending on a flag.
	 * If the flag is true, creates the successor pair, consisting of the
	 * successors of the first and second projections of the original pair.
	 * Otherwise, creates the predecessor pair, consisting of the 
	 * predecessors of the first and second projections of the original pair.
	 * @param pair the original pair.
	 * @param type the parametric type of the pair.
	 * @param more indicates the successor (true) or predecessor (false).
	 * @return the successor, or predecessor of the pair, if any.
	 */
	protected Object getNextPair(Entry<?, ?> pair, String type, 
			boolean more) {
		String keyType = getKeyType(type);
		String valueType = getValueType(type);
		if (more) {
			return new SimpleEntry<Object, Object> (
					getSuccessor(pair.getKey(), keyType),
					getSuccessor(pair.getValue(), valueType));
		}
		else {
			return new SimpleEntry<Object, Object> (
					getPredecessor(pair.getKey(), keyType),
					getPredecessor(pair.getValue(), valueType));
		}
	}

	
}
