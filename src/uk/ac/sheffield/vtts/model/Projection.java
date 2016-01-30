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

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

/**
 * Projection represents a function creating, or projecting from, a tuple.
 * There are three projection operators, whose names are:  pair, first and 
 * second.  The only supported tuple-kind is a pair of values, having the
 * model type Pair[K, V] for some types K and V.  Pairs are used as maplets
 * in Map types.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Projection extends Function {

	/**
	 * The pair-type Pair[K, V]
	 */
	private String pairType;
	
	/**
	 * The first projection type, the key type K.
	 */
	private String keyType;
	
	/**
	 * The second projection type, the value type V.
	 */
	private String valueType;
	
	/**
	 * Checks the name of this Projection expression and sets the number
	 * of operands to one for a projection and two for a pair construction.
	 */
	@Override
	protected void nameCheck() throws SemanticError {
		String legalNames = "pair,first,second,";
		if (! legalNames.contains(name + ','))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("pair"))
			maxOperands = 2;
		else
			maxOperands = 1;
	}

	/**
	 * Checks that the types of the operands and result are consistent.  If 
	 * the operator is "pair", checks that the result is of a pair-type and
	 * the arguments of the corresponding key-type and value-type.  Otherwise
	 * checks that the argument is of a pair-type, and that the projections
	 * are of the corresponding key-type and value-type.  Caches the 
	 * pairType, keyType and valueType  for use during binding and value 
	 * synthesis.  
	 */
	@Override
	protected void typeCheck() throws SemanticError {
		if (name.equals("pair")) 
			pairType = getType();
		else
			pairType = operand(0).getType();
		keyType = factory.getKeyType(pairType);
		valueType = factory.getValueType(pairType);
		if (! factory.getBaseType(pairType).equals("Pair"))
			semanticError("has an illegal pair-type '" + pairType + "'.");
		if (name.equals("pair")) {
			String op0Type = operand(0).getType();
			if (! op0Type.equals(keyType))
				semanticError("has an illegal key-type '" + op0Type +
						"' for operand(0).");
			String op1Type = operand(1).getType();
			if (! op1Type.equals(valueType))
				semanticError("has an illegal value-type '" + op1Type +
						"' for operand(1).");
		}
		else if (name.equals("first")) {
			String resultType = getType();
			if (! resultType.equals(keyType))
				semanticError("has an illegal result type: '" + 
						resultType + "'.");
		}
		else {  // name == "second"
			String resultType = getType();
			if (! resultType.equals(valueType))
				semanticError("has an illegal result type: '" + 
						resultType + "'.");
		}
	}
	
	/**
	 * Creates a default Projection.
	 */
	public Projection() {
	}
	
	/**
	 * Creates a Projection with the given operator name.
	 * @param name the projection operator name.
	 */
	public Projection(String name) {
		super(name);
	}
	
	/**
	 * Creates a Projection with the given operator name and result type.
	 * @param name the projection operator name.
	 * @param type the result type of this operator.
	 */
	public Projection(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Returns the result type of this Projection.  Returns the explicitly
	 * given type, otherwise infers this type by a heuristic.  If the 
	 * operator is "pair", infers the pair-type from the types of the two
	 * operands.  Otherwise, infers the "first" or "second" projection type
	 * from the type of the sole operand.  Caches the inferred type.
	 * @return the type of this Projection.
	 * @throws SemanticError if no type can be inferred.
	 */
	@Override
	public String getType() {
		if (type == null) {
			if (name.equals("pair") && expressions.size() == 2)
				type = "Pair[" + operand(0).getType() + ", " 
						+ operand(1).getType() + "]";
			else if (expressions.size() == 1) {
				String opType = operand(0).getType();
				if (name.equals("first"))
					type = factory.getKeyType(opType);
				else  // name.equals("second")
					type = factory.getValueType(opType);
			}
			else
				semanticError("cannot infer type for '" + name + "'.");
		}
		return type;
	}

	/**
	 * Evaluates this Projection expression.  Dispatches internally on the
	 * name of the operator to one of the pair-operations.
	 */
	@Override
	public Object evaluate() {
		typeCheck();
		Object value0 = operand(0).evaluate();
		if (name.equals("pair")) {
			Object value1 = operand(1).evaluate();
			return pair(value0, value1);
		}
		else if (name.equals("first"))
			return first(value0);
		else
			return second(value0);
	}
	
	/**
	 * Returns a new ordered pair, consisting of the first and second objects.
	 * @param first the first projection.
	 * @param second the second projection.
	 * @return the pair containing the first and second projections.
	 */
	protected Entry<Object, Object> pair(Object first, Object second) {
		return new SimpleEntry<Object, Object>(first, second);
	}
	
	/**
	 * Returns the first projection from an ordered pair.
	 * @param pair the ordered pair.
	 * @return the first projection, the object ordered first.
	 */
	protected Object first(Object pair) {
		return ((Entry<?, ?>) pair).getKey();
	}
	
	/**
	 * Returns the second projection from an ordered pair.
	 * @param pair the ordered pair.
	 * @return the second projection, the object ordered second.
	 */
	protected Object second(Object pair) {
		return ((Entry<?, ?>) pair).getValue();
	}
	
	/**
	 * Rebinds the unbound parameters in this Projection expression, so that
	 * it yields the given expected result.  
	 */
	@Override
	public void rebind(Object result) {
		typeCheck();
		if (! operand(0).isBound()) {
			if (name.equals("pair")) {
				operand(0).rebind(first(result));
				if (! operand(1).isBound())  // If bound, should equal this
					operand(1).rebind(second(result));
			}
			else if (name.equals("first")) {
				Object value1 = factory.createObject(valueType);
				operand(0).rebind(pair(result, value1));
			}
			else {  // name == "second"
				Object value0 = factory.createObject(keyType);
				operand(0).rebind(pair(value0, result));
			}
		}
	}
	

}
