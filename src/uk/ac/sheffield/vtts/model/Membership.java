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

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Membership represents a cardinality or membership predicate on a 
 * collection.  There are six membership operators:  isEmpty, notEmpty,
 * includes, excludes, includesAll, excludesAll.  The first operand is 
 * expected to be some kind of collection.  The second operand is either
 * absent (isEmpty, notEmpty), or an element (includes, excludes), or 
 * another collection (includesAll, excludesAll), or an arbitrary key 
 * (includesKey, excludesKey).
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Membership extends Predicate {
	
	/**
	 * Flag set to true if the tested collection is a Map.  Otherwise, the
	 * tested collection is a List, or a Set.
	 */
	private boolean isMap = false;
	
	/**
	 * The whole parametric type of the List[T], Set[T] or Map[K, V].
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
	 * Checks the name of this Membership predicate and sets the expected
	 * number of operands.  All Membership predicates expect two operands,
	 * apart from isEmpty and notEmpty, which expect one operand.
	 */
	@Override
	protected void nameCheck() {
		String legalNames = "isEmpty,notEmpty,includes,excludes," +
				"includesAll,excludesAll,includesKey,excludesKey,";
		if (! legalNames.contains(name + ','))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("isEmpty") || name.equals("notEmpty"))
			maxOperands = 1;
		else
			maxOperands = 2;
	}
	
	/**
	 * Checks that the operand types are consistent and that the result type
	 * is Boolean.  The operand(0) type must be a collection.  Depending on
	 * the operation, the operand(1) type must either be the same collection
	 * type; or the value-type, or the key-type of operand(0).  Caches the
	 * value of paramType, valueType, keyType and pairType for use during 
	 * binding and value synthesis.  Caches the kind of collection, for 
	 * dispatching during evaluation.
	 */
	protected void typeCheck() {
		String legalTypes = "List,Set,Map";
		if (! getType().equals("Boolean"))
			semanticError("has an illegal result type '" + type + "'.");
		paramType = operand(0).getType();
		if (! legalTypes.contains(factory.getBaseType(paramType)))
			semanticError("has an illegal collection type '" + 
					paramType + "' for operand(0).");
		isMap = paramType.startsWith("Map");
		valueType = factory.getValueType(paramType);
		keyType = factory.getKeyType(paramType);  // K or Integer
		pairType = factory.getPairType(paramType); // Pair[K, V] or Pair[Integer, V]
		if (name.endsWith("des")) {
			// name == "includes" || "excludes"
			String elemType = (isMap ? pairType : valueType);
			String op1ElemType = operand(1).getType();
			if (! op1ElemType.equals(elemType))
				semanticError("has an illegal element value type '" +
						op1ElemType + "' for operand(1).");
		}
		else if (name.endsWith("All")) {
			// name == "includesAll" || "excludesAll"
			String op1GroupType = operand(1).getType();
			if (! paramType.equals(op1GroupType))
				semanticError("has an illegal collection type '" +
					op1GroupType + "' for operand(1).");
		}
		else if (name.endsWith("Key")) {
			// name == "includesKey" || "excludesKey"
			String op1KeyType = operand(1).getType();
			if (! keyType.equals(op1KeyType))
				semanticError("has an illegal search key type '" +
					op1KeyType + "' for operand(1).");
		}
	}
	
	/**
	 * Creates a default Membership predicate.
	 */
	public Membership() {
	}
	
	/**
	 * Creates a named Membership predicate.  Ensures that the name is one
	 * of the allowed membership operators.
	 * @param name the name of the membership predicate.
	 */
	public Membership(String name) {
		super(name);
	}
	
	/**
	 * Reports whether this Membership predicate is consistent.  Returns 
	 * false in the	four cases:  includes(bottom, x), includesAll(bottom, y),
	 * includesKey(bottom, z) and notEmpty(bottom), where bottom is the
	 * Constant representing an empty collection.  Otherwise returns true.
	 * @return true if this Membership predicate is consistent.
	 */
	@Override
	public boolean isConsistent() {
		if (operand(0).isBottom())
			return ! (name.equals("notEmpty") ||
					name.startsWith("includes"));
		else
			return true;
	}
	
	/**
	 * Tests whether this Membership predicate subsumes the other Predicate.
	 * If the other is also a Membership predicate, tests whether this
	 * subsumes the other.  If the other is an AND-Proposition, tests whether
	 * this Membership predicate subsumes one of the other's conjuncts.  
	 * Otherwise returns false.
	 * @param other the other Predicate.
	 * @return true if this subsumes the other Predicate.
	 */
	@Override
	public boolean subsumes(Predicate other) {
		if (other instanceof Membership)
			return subsumesMembership((Membership) other);
		else if (other instanceof Proposition)
			return subsumesProposition((Proposition) other);
		else 
			return other.isFalseConstant();  // Degenerate case.
	}

	/**
	 * Reports whether this Membership predicate subsumes the other.  This
	 * is only true if the predicates are equal (we cannot currently reason
	 * about includesAll subsuming includes).
	 * @param other the other Membership predicate.
	 * @return true, if this Membership subsumes the other Membership.
	 */
	protected boolean subsumesMembership(Membership other) {
		return equals(other);
	}

	/**
	 * Returns the complement of this Membership predicate.  Converts 
	 * pairwise between the predicates isEmpty/notEmpty, includes/excludes
	 * and includesKey/excludesKey.  The two predicates includesAll and 
	 * excludesAll cannot be so simply complemented, so are wrapped in a
	 * negated Proposition.
	 * @return the logical complement of this Membership predicate.
	 */
	@Override
	public Predicate negate() {
		if (name.endsWith("All"))
			return super.negate();
		else {
			Membership result = new Membership(getNegatedName());
			for (Expression operand : expressions) {
				result.addExpression(operand);
			}
			return result;
		}
	}

	/**
	 * Algorithm for returning the negated name of this Membership.
	 * Branch according to three-letter unique sequences for pairs of
	 * operations.
	 * @return the negated name.
	 */
	private String getNegatedName() {
		if (name.endsWith("pty"))
			if (name.startsWith("not"))
				return "isEmpty";
			else
				return "notEmpty";
		else if (name.endsWith("des"))
			if (name.startsWith("inc"))
				return "excludes";
			else
				return "includes";
		else if (name.endsWith("All"))
			if (name.startsWith("inc"))
				return "excludesAll";
			else
				return "includesAll";
		else // (name.endsWith("Key"))
			if (name.startsWith("inc"))
				return "excludesKey";
			else
				return "includesKey";
	}

	/**
	 * Executes this Membership predicate on its operands.  Checks the 
	 * consistency of the operand types and then branches according to the
	 * number of operands, and the name of this Membership predicate.
	 * @return true, if the predicate holds between the operands.
	 */
	public Boolean evaluate() {
		typeCheck();
		Object value0 = operand(0).evaluate();
		if (maxOperands == 1) {
			if (name.equals("isEmpty"))
				return isEmpty(value0);
			else // name.equals("notEmpty")
				return notEmpty(value0);
		}
		else {  // maxOperands = 2
			Object value1 = operand(1).evaluate();
			if (name.equals("includes"))
				return includes(value0, value1);
			else if (name.equals("excludes"))
				return excludes(value0, value1);
			else if (name.equals("includesAll"))
				return includesAll(value0, value1);
			else if (name.equals("excludesAll"))
				return excludesAll(value0, value1);
			else if (name.equals("includesKey"))
				return includesKey(value0, value1);
			else // name.equals("excludesKey")
				return excludesKey(value0, value1);
		}
	}

	/**
	 * Reports whether the collection is empty.  Casts the argument to a
	 * Collection or a Map, and then calls Java's built-in method isEmpty().
	 * @param collection any kind of collection.
	 * @return true, if the collection is empty.
	 */
	protected Boolean isEmpty(Object collection) {
		if (isMap)
			return ((Map<?,?>) collection).isEmpty();
		else
			return ((Collection<?>) collection).isEmpty();
	}

	/**
	 * Reports whether the collection is not empty.  Returns the logical
	 * negation of:  isEmpty(collection).
	 * @param collection any kind of collection.
	 * @return true, if the collection is not empty.
	 */
	protected Boolean notEmpty(Object collection) {
		return ! isEmpty(collection);
	}

	/**
	 * Reports whether the collection includes the value in its elements.
	 * If the collection argument is a List or a Set, casts this to a
	 * Collection then calls Java's built-in method contains(); otherwise
	 * casts it to a Map, extracts the entrySet() and tests whether this
	 * contains() the supplied entry.
	 * @param collection any kind of collection.
	 * @param value any value of the collection's value-type.
	 * @return true, if the value is an element of the collection.
	 */
	protected Boolean includes(Object collection, Object value) {
		if (isMap)
			return ((Map<?,?>) collection).entrySet().contains(value);
		else
			return ((Collection<?>) collection).contains(value);
	}

	/**
	 * Reports whether the collection excludes the value from its elements.
	 * Returns the logical negation of: includes(collection, value).
	 * @param collection any kind of collection.
	 * @param value any value of the collection's value-type.
	 * @return true, if the value is not an element of the collection.
	 */
	protected Boolean excludes(Object collection, Object value) {
		return ! includes(collection, value);
	}

	/**
	 * Reports whether the first collection includes all of the second one.
	 * If the first and second arguments are Lists or Sets, casts these both 
	 * to Collections, then calls Java's built-in method: containsAll();
	 * otherwise casts both arguments to Maps, extracts the entrySet()
	 * from each and tests for inclusion using containsAll().
	 * @param first the first collection of any kind.
	 * @param second the second collection of the same kind.
	 * @return true, if all elements of the second are also in the first 
	 * collection.
	 */
	protected Boolean includesAll(Object first, Object second) {
		if (isMap)
			return ((Map<?,?>) first).entrySet().containsAll(
					((Map<?,?>) second).entrySet());
		else
			return ((Collection<?>) first).containsAll(
					(Collection<?>) second);
	}

	/**
	 * Reports whether the first collection excludes all of the second one.
	 * If the first and second arguments are Lists or Sets, casts these both
	 * to Collections, then tests that no element from the second exists in
	 * the first, using Java's built-in method: contains(); otherwise, casts 
	 * both arguments to Maps, extracts the entrySet() from each, and tests
	 * whether the first excludes every entry of the second.
	 * @param first the first collection of any kind.
	 * @param second the second collection of the same kind.
	 * @return true, if no elements of the second are also in the first
	 * collection.
	 */
	protected Boolean excludesAll(Object first, Object second) {
		Collection<?> groupOne = (isMap ? ((Map<?,?>) first).entrySet() :
			(Collection<?>) first);
		Collection<?> groupTwo = (isMap ? ((Map<?,?>) second).entrySet() :
			(Collection<?>) second);
		for (Object element : groupTwo) {
			if (groupOne.contains(element))
				return false;
		}
		return true;
	}

	/**
	 * Reports whether the collection includes the key in its search keys.
	 * For a Map, casts the collection to a Map, then calls Java's built-in 
	 * method: containsKey().  For a list, casts the collection to a List 
	 * and the key to an Integer, then checks if this index is in range.
	 * @param collection must be a Map or a List.
	 * @param key a key of the Map's key-type, or an Integer index.
	 * @return true, if the key is present in the collection's search keys.
	 */
	protected Boolean includesKey(Object collection, Object key) {
		if (isMap)
			return ((Map<?, ?>) collection).containsKey(key);
		else
			// Assumes indices run from 1..n
			return ((Integer) key) > 0 && 
					((Integer) key) <= ((List<?>) collection).size();
	}

	/**
	 * Reports whether the map excludes the key from its search keys.
	 * Returns the logical negation of: includesKey(map, key).
	 * @param collection must be a Map or a List.
	 * @param key a key of the Map's key-type, or an Integer index.
	 * @return true, if the key is absent from the collection's search keys.
	 */
	protected Boolean excludesKey(Object collection, Object key) {
		return ! includesKey(collection, key);
	}

	/**
	 * Rebinds the unbound operands of this Membership predicate, so that it
	 * yields true.  If either operand is unbound, binds it to a value 
	 * satisfying the constraint with respect to the other operand.  If both
	 * are unbound, first binds the second operand to a default value.
	 */
	protected void rebindTrue() {
		if (! operand(0).isBound()) {
			if (name.equals("isEmpty"))
				operand(0).rebind(factory.createObject(paramType));
			else if (name.equals("notEmpty"))
				operand(0).rebind(factory.createObject("?", paramType));
			else {
				if (! operand(1).isBound())
					operand(1).rebind();  // To any default value.
				Object value1 = operand(1).evaluate();
				String string1 = value1.toString();
				if (name.equals("includes"))
					operand(0).rebind(factory.createObject(string1, paramType));
				else if (name.equals("includesAll"))
					operand(0).rebind(value1);
				else if (name.equals("includesKey"))
					operand(0).rebind(factory.createObject(string1, paramType));
				else // name == "excludes" || "excludesAll" || "excludesKey"
					operand(0).rebind(factory.createObject(paramType));
			}
		}
		else if (maxOperands == 2 && ! operand(1).isBound()) {
			// operand(0) is already bound.
			Object value0 = operand(0).evaluate();
			if (name.equals("includes"))
				operand(1).rebind(anyValueFrom(value0));
			else if (name.equals("excludes"))
				operand(1).rebind(noValueFrom(value0));
			else if (name.equals("includesAll"))
				operand(1).rebind(value0);
			else if (name.equals("excludesAll"))
				operand(1).rebind(factory.createObject(paramType));
			else if (name.equals("includesKey"))
				operand(1).rebind(anyKeyFrom(value0));
			else // name == "excludesKey"
				operand(1).rebind(noKeyFrom(value0));
		}
	}
	
	/**
	 * Rebinds the unbound operands of this Membership predicate, so that it
	 * yields false.  If either operand is unbound, binds it to a value that
	 * violates the constraint with respect to the other operand.  If both
	 * are unbound, first binds the second operand to a default value.
	 */
	@Override
	protected void rebindFalse() {
		if (! operand(0).isBound()) {
			if (name.equals("isEmpty"))
				operand(0).rebind(factory.createObject("?", paramType));
			else if (name.equals("notEmpty"))
				operand(0).rebind(factory.createObject(paramType));
			else {
				if (! operand(1).isBound())
					operand(1).rebind();  // To any default value.
				Object value1 = operand(1).evaluate();
				String string1 = value1.toString();
				if (name.equals("excludes"))
					operand(0).rebind(
							factory.createObject(string1, paramType));
				else if (name.equals("excludesAll"))
					operand(0).rebind(value1);
				else if (name.equals("excludesKey"))
					operand(0).rebind(factory.createObject(string1, paramType));
				else // name == "includes" || "includesAll" || "includesKey"
					operand(0).rebind(factory.createObject(paramType));
			}
		}
		else if (maxOperands == 2 && ! operand(1).isBound()) {
			// operand(0) is already bound.
			Object value0 = operand(0).evaluate();
			if (name.equals("includes"))
				operand(1).rebind(noValueFrom(value0));
			else if (name.equals("excludes"))
				operand(1).rebind(anyValueFrom(value0));
			else if (name.equals("includesAll"))
				operand(1).rebind(factory.createObject(paramType));
			else if (name.equals("excludesAll"))
				operand(1).rebind(value0);
			else if (name.equals("includesKey"))
				operand(1).rebind(noKeyFrom(value0));
			else // name == "excludesKey"
				operand(1).rebind(anyKeyFrom(value0));
		}
	}

	/**
	 * Factory method to extract, if possible, an element from a collection.
	 * Returns either the first element from the collection, if one exists,
	 * or a default element, if none can be found.
	 * @param collection any kind of collection.
	 * @return an element present in the collection.
	 */
	private Object anyValueFrom(Object collection) {
		if (isMap) {
			Map<?,?> map = (Map<?,?>) collection;
			if (map.isEmpty())
				return factory.createObject(pairType);
			else
				return map.entrySet().iterator().next();
		}
		else {
			Collection<?> group = (Collection<?>) collection;
			if (group.isEmpty())
				return factory.createObject(valueType);
			else
				return group.iterator().next();
		}
	}
	
	/**
	 * Factory method to synthesise, where possible, a novel element that is
	 * not present in the collection.  This method monotonically increments 
	 * the default element for the collection, until a novel element is found.
	 * @param collection any kind of collection.
	 * @return an element absent from the collection.
	 */
	private Object noValueFrom(Object collection) {
		if (isMap) {
			Map<?,?> map = (Map<?,?>) collection;
			Object key = factory.createObject(keyType);
			Object value = factory.createObject(valueType);
			while (map.containsKey(key))  // POSSIBLY DANGEROUS?
				key = factory.getSuccessor(key, keyType);
			String pair = key.toString() + "=" + value.toString();
			return factory.createObject(pair, pairType);
		}
		else {
			Collection<?> group = (Collection<?>) collection;
			Object value = factory.createObject(valueType);
			while (group.contains(value))  // POSSIBLY DANGEROUS?
				value = factory.getSuccessor(value, valueType);
			return value;
		}
	}
	
	/**
	 * Factory method to extract, where possible, a search key from a Map,
	 * or valid index from a List.
	 * Treating the argument as a Map, returns either the first key found
	 * in the map, or synthesises a default key (to avoid null values).
	 * Treating the argument as an indexed List, returns the last index
	 * of the List.
	 * @param collection any Map or List.
	 * @return a key present in the Map, or valid List index.
	 */
	private Object anyKeyFrom(Object collection) {
		if (isMap) {
			Map<?,?> map = (Map<?,?>) collection;
			if (map.isEmpty())
				return factory.createObject(keyType);
			else
				return map.keySet().iterator().next();
		}
		else {
			Collection<?> group = (Collection<?>) collection;
			return group.size();  // indices run from 1..n
		}
	}
	
	/**
	 * Factory method to synthesise, where possible, a novel search key that
	 * is not present in a Map, or invalid index in a List.
	 * Treating the argument as a Map, monotonically increments the default 
	 * key for the Map, until a novel key is found.  Treating the argument
	 * as a List, returns one more than the last valid index.
	 * @param collection any Map or List.
	 * @return a key absent from the Map, or the List's insertion point index.
	 */
	private Object noKeyFrom(Object collection) {
		if (isMap) {
			Map<?,?> map = (Map<?,?>) collection;
			Object key = factory.createObject(keyType);
			while (map.containsKey(key))  // POSSIBLY DANGEROUS?
				key = factory.getSuccessor(key, keyType);
			return key;
		}
		else {
			Collection<?> group = (Collection<?>) collection;
			return group.size() + 1;  // indices run from 1..n
		}
	}
	
}
