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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Manipulation represents an expression manipulating a collection of items.
 * The first operand is expected to be some kind of collection, whether a
 * Set, List or Map.  The remaining operands may be an element, an index,
 * a key, or another collection.  All Manipulations are functional, in that
 * they do not modify an operand directly, but return a modified copy.  If
 * a side-effect is intended, the result must be reassigned to the same
 * Parameter that stored the original collection, before the operation.
 * There are nine Manipulation operations, whose names include:  size, 
 * insert, remove, insertAll, removeAll, searchAt, replaceAt, insertAt
 * and removeAt.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Manipulation extends Function {
	
	/**
	 * Flag set to true if the tested collection is a Map.  Otherwise, the
	 * manipulated collection is a List, or a Set.
	 */
	private boolean isMap;
	
	/**
	 * Flag set to true if the tested collection is a List.  Otherwise, the
	 * manipulated collection is a Set or a Map.
	 */
	private boolean isList;

	/**
	 * The parametric type of the List, Set or Map manipulated by this 
	 * Manipulation.
	 * This value is cached when types are checked.
	 */
	private String paramType = null;
	
	/**
	 * The value type V of the List[V], Set[V] or Map[K, V].  
	 */
	private String valueType = null;
	
	/**
	 * The key type K of the Map[K, V].	
	 */
	private String keyType = null;
	
	/**
	 * The pair type Pair[K, V] of a Map[K, V].
	 */
	private String pairType = null;
		
	/**
	 * Checks the name of this Manipulation expression and sets the expected
	 * number of operands.  Manipulations expect one to three operands, the
	 * first of which is always a collection of items (whether List, Set or
	 * Map).  The operation "size" expects this single collection operand.  
	 * The operations "insert, remove" expect the second operand to be an
	 * element of the collection.  The operations "insertAll, removeAll" 
	 * expect the second operand to be another collection.  The operations
	 * "searchAt, removeAt" expect the second operand to be a key or index. 
	 * The operations "insertAt, replaceAt" expect a similar second operand
	 * and an element value as the third operand. 
	 */
	@Override
	protected void nameCheck() {
		String legalNames = "size,insert,remove,insertAll,removeAll," +
				"searchAt,replaceAt,insertAt,removeAt,";
		if (! legalNames.contains(name + ','))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("size"))
			maxOperands = 1;
		else if (name.equals("replaceAt") || name.equals("insertAt"))
			maxOperands = 3;
		else
			maxOperands = 2;
	}
	
	/**
	 * Checks that the operand and result types are consistent.  The first
	 * operand(0) must always be a collection type.  Depending on the named
	 * operation, the operand(1) type must either be the same collection
	 * type; or the value-type, or the key-type of operand(0).  If there is
	 * any operand(2), this is always the value-type.  The result-type must
	 * either be the collection-type, the value-type or "Integer", according
	 * to the operation.  Caches the values of paramType, valueType, keyType
	 * and pairType for use during binding and value synthesis.  Caches the
	 * kind of collection, for dispatching during evaluation.
	 */
	protected void typeCheck() {
		String legalTypes = "List,Set,Map";
		paramType = operand(0).getType();
		// Checking operand(0)
		if (! legalTypes.contains(factory.getBaseType(paramType)))
			semanticError("has an illegal collection type '" + 
					paramType + "' for operand(0).");
		isMap = paramType.startsWith("Map");
		isList = paramType.startsWith("List");
		valueType = factory.getValueType(paramType);
		keyType = factory.getKeyType(paramType);
		pairType = factory.getPairType(paramType); // Pair[K, V] or Pair[Integer, V]
		// Checking the result type
		String resultType = (name.equals("size") ? "Integer" :
				(name.equals("searchAt") ? valueType : paramType));
		if (! getType().equals(resultType))	
			semanticError("has an illegal result type '" + type + "'");
		// Checking the type of operand(1)
		if (name.equals("insert") || name.equals("remove")) {
			String elemType = (isMap ? pairType : valueType);
			String op1ElemType = operand(1).getType();
			if (! op1ElemType.equals(elemType))
				semanticError("has an illegal element value type '" +
						op1ElemType + "' for operand(1).");
		}
		else if (name.endsWith("All")) {
			// name == "insertAll" || "removeAll"
			String op1GroupType = operand(1).getType();
			if (! paramType.equals(op1GroupType))
				semanticError("has an illegal collection type '" +
					op1GroupType + "' for operand(1).");
		}
		else if (name.endsWith("At")) {
			// name == "searchAt" || "replaceAt" || "insertAt" || "removeAt"
			String op1KeyType = operand(1).getType();
			if (! op1KeyType.equals(keyType))
				semanticError("has an illegal search key type '" +
					op1KeyType + "' for operand(1).");
			if (name.equals("insertAt") || name.equals("replaceAt")) {
				// Checking the type of operand(2)
				String op2ValueType = operand(2).getType();
				if (! op2ValueType.equals(valueType))
					semanticError("has an illegal element value type '" +
						op2ValueType + "' for operand(2).");
			}
		}
	}

	/**
	 * Creates a default Manipulation expression.
	 */
	public Manipulation() {
	}

	/**
	 * Creates a named Manipulation expression.
	 * @param name the set-theoretic operator name.
	 */
	public Manipulation(String name) {
		super(name);
	}

	/**
	 * Creates a named and typed Manipulation expression.
	 * @param name the name of the set-theoretic operator.
	 * @param type the result type of the expression.
	 */
	public Manipulation(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Returns the result type of this Manipulation.  Returns the explicitly
	 * given type, otherwise infers this type by a heuristic.   By default,
	 * returns the type of the first operand; but if the operator is "size",
	 * returns "Integer" and if the operator is "searchAt", returns the
	 * associated value-type.
	 * @return the type of this Manipulation.
	 * @throws SemanticError if no type can be inferred.
	 */
	@Override
	public String getType() {
		if (type == null) {
			if (expressions.size() > 0) {
				type = operand(0).getType();	// default type
				if (name.equals("size"))
					type = "Integer";
				else if (name.equals("searchAt"))
					type = factory.getValueType(type);
			}
			else
				semanticError("cannot infer type for '" + name + "'.");
		}
		return type;
	}

	/**
	 * Executes this Manipulation on its operands.  Expects one operand for
	 * the size() operation, three operands for insertAt(), replaceAt() and
	 * two operands for all other operations.  Checks the consistency of the 
	 * operand types and then branches according to the number of operands, 
	 * and the name of this Manipulation operation.  In keeping with the
	 * side-effect free specification style, all modifications to an input
	 * collection return a new output collection in which the changes have
	 * been made.  All indexing is from 1..n (rather than 0..n-1).
	 * @return an integer size, a found element, or a new collection.
	 */
	public Object evaluate() {
		typeCheck();
		Object value0 = operand(0).evaluate();
		if (maxOperands == 1) {
			// name == "size"
			return size(value0);
		}
		else {
			Object value1 = operand(1).evaluate();
			if (maxOperands == 2) {
				if (name.equals("searchAt"))
					return searchAt(value0, value1);
				else if (name.equals("insert"))
					return insert(value0, value1);
				else if (name.equals("remove"))
					return remove(value0, value1);
				else // name == "removeAt"
					return removeAt(value0, value1);
			}
			else {
				Object value2 = operand(2).evaluate();
				if (name.equals("insertAt"))
					return insertAt(value0, value1, value2);
				else // name == "replaceAt"
					return replaceAt(value0, value1, value2);
			}
		}
	}
	
	/**
	 * Returns the size or cardinality of a collection.  Casts the argument
	 * to a Map or a Collection, and calls Java's built-in size() method.
	 * @param collection the collection.
	 * @return the size of the collection.
	 */
	protected Integer size(Object collection) {
		if (isMap)
			return ((Map<?, ?>) collection).size();
		else
			return ((Collection<?>) collection).size();
	}
	
	/**
	 * Returns a new collection in which the value has been inserted.  For a
	 * Map, expects the value to be an Entry tuple; otherwise the value is of
	 * the value-type of the List or Set.  Maps and Sets insert their values
	 * uniquely at unordered positions; whereas a Set inserts the value in
	 * final position.
	 * @param collection the input collection.
	 * @param value the value to insert.
	 * @return the output collection containing the inserted value.
	 */
	protected Object insert(Object collection, Object value) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) collection;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			Entry<?,?> entry = (Entry<?,?>) value;
			output.put(entry.getKey(), entry.getValue());
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) collection;
			List<Object> output = new ArrayList<Object>(input);
			output.add(value);
			return output;
		}
		else {
			Set<?> input = (Set<?>) collection;
			Set<Object> output = new HashSet<Object>(input);
			output.add(value);
			return output;		
		}
	}
	
	/**
	 * Returns a new collection in which the value has been removed.  For a
	 * Map, expects the value to be an Entry tuple; otherwise the value is of
	 * the value-type of the List or Set.  Removes the unique occurrence of
	 * the value from a Map or Set, or the first occurrence from a List.
	 * @param collection the input collection.
	 * @param value the value to remove.
	 * @return the output collection missing the removed value.
	 */
	protected Object remove(Object collection, Object value) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) collection;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			Entry<?,?> entry = (Entry<?,?>) value;
			output.remove(entry.getKey());
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) collection;
			List<Object> output = new ArrayList<Object>(input);
			output.remove(value);
			return output;
		}
		else {
			Set<?> input = (Set<?>) collection;
			Set<Object> output = new HashSet<Object>(input);
			output.remove(value);
			return output;		
		}
	}
	
	/**
	 * Returns a new collection containing all of the first and second 
	 * collections.  For a List, appends the second to the first List; for a
	 * Set, returns the union of the first and second Sets; and for a Map,
	 * returns the union with override of the first and second Maps, in which
	 * entries from the second may replace entries from the first, if they
	 * have the same key. 
	 * @param first the first collection.
	 * @param second the second collection.
	 * @return a new collection containing the pooled elements.
	 */
	protected Object insertAll(Object first, Object second) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) first;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			output.putAll((Map<?,?>) second);
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) first;
			List<Object> output = new ArrayList<Object>(input);
			output.addAll((List<?>) second);
			return output;
		}
		else {
			Set<?> input = (Set<?>) first;
			Set<Object> output = new HashSet<Object>(input);
			output.addAll((Set<?>) second);
			return output;		
		}
	}
	
	/**
	 * Returns a new collection containing those elements from the first, 
	 * which were not present in the second collection.  For a Set or Map,
	 * returns the set-difference of the two Sets or Maps; for a List, 
	 * returns the bag-difference of the two Lists, preserving element 
	 * order.
	 * @param first the first collection.
	 * @param second the second collection.
	 * @return a new collection subtracting all of the second from the first.
	 */
	protected Object removeAll(Object first, Object second) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) first;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			output.entrySet().removeAll(((Map<?,?>) second).entrySet());
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) first;
			List<Object> output = new ArrayList<Object>(input);
			output.removeAll((List<?>) second);
			return output;
		}
		else {
			Set<?> input = (Set<?>) first;
			Set<Object> output = new HashSet<Object>(input);
			output.removeAll((Set<?>) second);
			return output;		
		}
	}
	
	/**
	 * Returns a new collection, in which a value is inserted opposite a key
	 * or index.  For a Map, stores the value opposite the key.  If the key
	 * already exists in the Map, replaces the associated value.  For a List,
	 * inserts the value at the key index, so long as the index is in range.
	 * Indexing is 1..size; and the insertion range allows size+1.
	 * This method is not defined for Sets.
	 * @param collection the input Map or List.
	 * @param key a key or Integer index.
	 * @param value the value to insert.
	 * @return a new collection, in which the value is present, indexed by 
	 * the key.
	 */
	protected Object insertAt(Object collection, Object key, Object value) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) collection;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			output.put(key, value);
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) collection;
			List<Object> output = new ArrayList<Object>(input);
			output.add(((Integer) key) - 1, value);
			return output;
		}
		else {
			semanticError("'insertAt' not defined for 'Set' type.");
			return null;
		}
	}
	
	/**
	 * Returns a new collection, in which a value is removed opposite a key
	 * or index.  For a Map, removes the Map entry indexed by the key, if
	 * such an entry exists.  For a List, removes the value at the key index,
	 * so long as the index is in range.  Indexing is 1..size.
	 * This method is not defined for Sets.
	 * @param collection the input Map or List.
	 * @param key a key or Integer index.
	 * @return a new collection, in which the value originally indexed by 
	 * the key is absent.
	 */
	protected Object removeAt(Object collection, Object key) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) collection;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			output.remove(key);
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) collection;
			List<Object> output = new ArrayList<Object>(input);
			output.remove(((Integer) key) - 1);
			return output;
		}
		else {
			semanticError("'removeAt' not defined for 'Set' type.");
			return null;
		}
	}
	
	/**
	 * Returns a new collection, in which a value is replaced opposite a key
	 * or index.  For a Map, replaces the value opposite the key.  If the key
	 * is not present in the Map, inserts a Map entry for the key and value.
	 * For a List, replaces the value at the key index, so long as the index 
	 * is in range.  Indexing is 1..size.
	 * This method is not defined for Sets.
	 * @param collection the input Map or List.
	 * @param key a key or Integer index.
	 * @param value the value to replace.
	 * @return a new collection, in which the value is present, indexed by 
	 * the key.
	 */
	protected Object replaceAt(Object collection, Object key, Object value) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) collection;
			Map<Object, Object> output = new HashMap<Object, Object>(input);
			output.put(key, value);
			return output;
		}
		else if (isList) {
			List<?> input = (List<?>) collection;
			List<Object> output = new ArrayList<Object>(input);
			output.set(((Integer) key) - 1, value);
			return output;
		}
		else {
			semanticError("'replaceAt' not defined for 'Set' type.");
			return null;
		}
	}

	/**
	 * Searches a collection and returns the object found opposite a given
	 * key or index.  For a Map, returns the value associated with the key,
	 * if one exists (null may be returned if not).  For a List, returns the
	 * value stored at the key index, so long as the index is in range.
	 * Indices run from 1..n.
	 * This method is not defined for Sets.
	 * @param collection the input Map or List.
	 * @param key a key or Integer index.
	 * @return the value found opposite the key or index.
	 */
	protected Object searchAt(Object collection, Object key) {
		if (isMap) {
			Map<?,?> input = (Map<?,?>) collection;
			return input.get(key);
		}
		else if (isList) {
			List<?> input = (List<?>) collection;
			return input.get(((Integer) key) -1);
		}
		else {
			semanticError("'searchAt' not defined for 'Set' type.");
			return null;
		}
	}
	
	/**
	 * Rebinds the unbound operands of this Manipulation expression, so that it
	 * yields the given expected result.
	 * @param result the expected result of this Manipulation expression.
	 */
	@Override
	public void rebind(Object result) {
		typeCheck();  // Cache the paramType
		if (! operand(0).isBound()) {
			// Try to bind operand(0), given bound values for the rest.
			if (maxOperands == 1) {  // name == "size"
				Object value0 = createWithSize((Integer) result);
				operand(0).rebind(value0);
			}
			else {  // maxOperands > 1
				if (! operand(1).isBound())
					operand(1).rebind();  // to any default value
				Object value1 = operand(1).evaluate();
				if (name.equals("searchAt")) {
					Object value0 = createWithIndex(value1);
					operand(0).rebind(replaceAt(value0, value1, result));
				}
				else if (name.equals("insert"))
					operand(0).rebind(remove(result, value1));
				else if (name.equals("remove"))
					operand(0).rebind(insert(result, value1));
				else if (name.equals("insertAll"))
					operand(0).rebind(removeAll(result, value1));
				else if (name.equals("removeAll"))
					operand(0).rebind(insertAll(result, value1));
				else if (name.equals("insertAt"))
					operand(0).rebind(removeAt(result, value1));
				else {
					Object value2 = factory.createObject(valueType); // dummy
					if (name.equals("removeAt"))
						operand(0).rebind(insertAt(result, value1, value2));
					else  // name == "replaceAt"
						operand(0).rebind(replaceAt(result, value1, value2));
				}
			}
		}
		else if (maxOperands > 1 && ! operand(1).isBound()) {
			// Try to bind operand(1), given that operand(0) is bound
			Object value0 = operand(0).evaluate();
			if (name.equals("searchAt"))
				operand(1).rebind(getKeyFor(value0, result));
			else if (name.equals("insert"))
				operand(1).rebind(getDifference(result, value0));
			else if (name.equals("remove"))
				operand(1).rebind(getDifference(value0, result));
			else if (name.equals("insertAll"))
				operand(1).rebind(removeAll(result, value0));
			else if (name.equals("removeAll"))
				operand(1).rebind(removeAll(value0, result));
			else if (name.equals("removeAt")) {
				Object value2 = getDifference(value0, result);
				operand(1).rebind(getKeyFor(result, value2));
			}
			else {  // name == "insertAt" || name == "replaceAt"
				Object value2 = operand(2).isBound() ? operand(2).evaluate()
						: getDifference(result, value0);
				operand(1).rebind(getKeyFor(result, value2));
			}
		}
		else if (maxOperands > 2 && ! operand(2).isBound()) {
			// Try to bind operand(2), given that the others are bound
			Object value1 = operand(1).evaluate();
			// name == "insertAt" || name == "replaceAt"
			operand(2).rebind(searchAt(result, value1));
		}
	}
	
	/**
	 * Returns the search key or index from the collection, opposite which
	 * the result was found.  For a Map, iterates through the keys until one
	 * is found that maps to the result.  For a List, searches for the first
	 * index of the result.  Indices run from 1..n.
	 * @param collection the collection.
	 * @param result the found result.
	 * @return the search key or index.
	 */
	private Object getKeyFor(Object collection, Object result) {
		if (isMap) {
			Map<?,?> map = (Map<?,?>) collection;
			for (Object key : map.keySet()) {
				if (map.get(key).equals(result))
					return key;
			}
			return null;  // SHOULD NEVER HAPPEN?
		}
		else if (isList) {
			List<?> list = (List<?>) collection;
			return (Integer) list.indexOf(result) + 1;
		}
		else {
			semanticError("'searchAt' not defined for 'Set' type.");
			return null;
		}
	}
	
	/**
	 * Returns the single element present in the larger collection that is
	 * absent from the smaller collection.  First computes the set-difference
	 * between the two collections (treating a Map as a set of pairs), then
	 * extracts the singleton value from the difference-set.
	 * @param larger the larger collection.
	 * @param smaller the smaller collection.
	 * @return the extra value in the larger collection.
	 */
	private Object getDifference(Object larger, Object smaller) {
		Object difference = removeAll(larger, smaller);
		if (isMap) {
			Map<?,?> map = (Map<?,?>) difference;
			return map.entrySet().iterator().next();
		}
		else {
			Collection<?> group = (Collection<?>) difference;
			return group.iterator().next();
		}
	}
	
	/**
	 * Creates a populated collection of the given size.  Creates a default
	 * empty collection.  Then, for a Map, inserts a succession of unique 
	 * Pairs; and for a List or Set, inserts a unique succession of elements
	 * into the result.
	 * @param size the desired size for the collection.
	 * @return a suitably populated collection.
	 */
	private Object createWithSize(Integer size) {
		//DEBUG
		//System.out.print("         binding list size = " + size);
		String elemType = (isMap ? pairType : valueType);
		Object collection = factory.createObject(paramType);
		Object element = factory.createObject(elemType);
		while (size(collection) < size) {
			// Re-bind collection to the result of insertion
			collection = insert(collection, element);
			element = factory.getSuccessor(element, elemType);
		}
		//DEBUG
		//System.out.println("; ... actual size = " + size(collection));
		return collection;
	}
	
	/**
	 * Creates a populated collection, such that the given index or key is 
	 * valid.  For a Map, expects the index to be a Map key and creates a 
	 * singleton Map containing a Pair with this key and a default value.
	 * For a List, the index must be an Integer and this method returns the
	 * same as the result of createWithSize().
	 * @param index a List index, or a Map key.
	 * @return a suitably populated collection.
	 */
	private Object createWithIndex(Object index) {
		if (isMap) {
			// Relies on pair-construction with key only supplied.
			return factory.createObject(index.toString(), paramType);
		}
		else
			return createWithSize((Integer) index);
	}

}
