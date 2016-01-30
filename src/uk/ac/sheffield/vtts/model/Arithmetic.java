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


/**
 * Arithmetic represents a numerical function with an arithmetic operator
 * root node.  The operands are expected to be numbers.  There are six 
 * arithmetic operators, whose names include:  plus, minus, times, divide,
 * modulo and negate.  There are six legal numeric types, including: Byte,
 * Short, Integer, Long, Float and Double.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Arithmetic extends Function {
	
	/**
	 * Checks the name of an Arithmetic expression and sets the expected
	 * number of operands.  All arithmetic operators expect two operands,
	 * apart from "negate", which expects one operand.
	 */
	protected void nameCheck() {
		final String legalNames = "plus,minus,times,divide,modulo,negate,";
		if (! legalNames.contains(name + ','))  // need terminator
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("negate"))
			maxOperands = 1;
		else
			maxOperands = 2;
	}

	/**
	 * Checks the types of the operands of this Arithmetic expression.
	 * The operands must have one of the six legal numerical types; and if
	 * there are two operands, these must be of the same type.  The result
	 * must be of the same type as the operands.  Caches the result type,
	 * which is used as a dispatching token during evaluation.
	 */
	protected void typeCheck() {
		String legalTypes = "Integer,Double,Long,Float,Short,Byte,";
		if (! legalTypes.contains(getType() + ','))  // Caches type.
			semanticError("has an illegal result type '" + type + "'.");
		String lastType = null;
		for (Expression operand : getExpressions()) {
			String opType = operand.getType();
			if (! legalTypes.contains(opType))
				semanticError("has an illegal operand type '" + opType + "'.");
			if (lastType != null && !opType.equals(lastType))
				semanticError("has operands of conflicting  types '"
						+ lastType + ", " + opType + "'.");
			lastType = opType;
		}
		if (! lastType.equals(type))
			semanticError("has conflicting operand and result types '"
					+ lastType + ", " + type + "'.");
	}
	
	/**
	 * Creates a default Arithmetic expression.
	 */
	public Arithmetic() {
	}

	/**
	 * Creates a named Arithmetic expression.
	 * @param name the arithmetic operator name.
	 */
	public Arithmetic(String name) {
		super(name);
	}

	/**
	 * Creates a named and typed Arithmetic expression.
	 * @param name the name of the arithmetic operator.
	 * @param type the result type of the expression.
	 */
	public Arithmetic(String name, String type) {
		super(name, type);
	}

	/**
	 * Executes this Arithmetic on its operands.  Expects one operand for a
	 * negation, and two operands otherwise.  Checks the consistency of the
	 * types of the operands and result, then branches according to the name
	 * of the arithmetical operator.
	 * @return the arithmetical result, as a Number.
	 */
	public Number evaluate() {
		typeCheck();  // Caches the result type.
		Object value0 = operand(0).evaluate();
		if (name.equals("negate"))
			return negate(value0);
		else {
			Object value1 = operand(1).evaluate();
			if (name.equals("plus"))
				return plus(value0, value1);
			else if (name.equals("minus"))
				return minus(value0, value1);
			else if (name.equals("times"))
				return times(value0, value1);
			else if (name.equals("divide"))
				return divide(value0, value1);
			else if (name.equals("modulo"))
				return modulo(value0, value1);
			else
				return null;  // Never reached.
		}
	}
	
	/**
	 * Rebinds the unbound operands of this Arithmetic expression, so that it
	 * yields the given expected result.
	 * @param result the expected result of this Arithmetic expression.
	 */
	@Override
	public void rebind(Object result) {
		typeCheck();  // Cache the result type.
		if (name.equals("negate")) {
			if (! operand(0).isBound())
				operand(0).rebind(negate(result));
		}
		else {
			if (! operand(0).isBound()) {
				if (! operand(1).isBound())
					operand(1).rebind();  // to any default value, if unbound
				Object value = operand(1).evaluate();
				if (name.equals("plus"))
					operand(0).rebind(minus(result, value));
				else if (name.equals("minus"))
					operand(0).rebind(plus(result, value));
				else if (name.equals("times"))
					operand(0).rebind(divide(result, value));
				else if (name.equals("divide"))
					operand(0).rebind(times(result, value));
				else // if (name.equals("modulo"))
					operand(0).rebind(plus(result, value)); // guessed
			}
			else if (! operand(1).isBound()) {
				// operand(0) is bound already
				Object value = operand(0).evaluate();
				if (name.equals("plus"))
					operand(1).rebind(minus(result, value));
				else if (name.equals("minus"))
					operand(1).rebind(minus(value, result));
				else if (name.equals("times"))
					operand(1).rebind(divide(result, value));
				else if (name.equals("divide"))
					operand(1).rebind(divide(value, result));
				else // if (name.equals("modulo"))
					operand(1).rebind(minus(value, result)); // guessed
			}
		}
	}
	
	/**
	 * Computes the negation of a numerical value.
	 * @param value a numerical value. 
	 * @return the negation of this value.
	 */
	protected Number negate(Object value) {
		if (type.equals("Integer"))
			return - (Integer) value;
		else if (type.equals("Double"))
			return - (Double) value;
		else if (type.equals("Long"))
			return - (Long) value;
		else if (type.equals("Float"))
			return - (Float) value;
		else if (type.equals("Short"))
			return - (Short) value;
		else // if (type.equals("Byte"))
			return - (Byte) value;
	}

	/**
	 * Computes the sum of two numerical values. 
	 * @param first the first numerical value.
	 * @param second the second numerical value.
	 * @return the sum of the two values.
	 */
	protected Number plus(Object first, Object second) {
		if (type.equals("Integer"))
			return (Integer) first + (Integer) second;
		else if (type.equals("Double"))
			return (Double) first + (Double) second;
		else if (type.equals("Long"))
			return (Long) first + (Long) second;
		else if (type.equals("Float"))
			return (Float) first + (Float) second;
		else if (type.equals("Short"))
			return (Short) first + (Short) second;
		else // if (type.equals("Byte"))
			return (Byte) first + (Byte) second;
	}
	
	/**
	 * Computes the difference of two numerical values. 
	 * @param first the first numerical value.
	 * @param second the second numerical value.
	 * @return the difference of the first minus the second value.
	 */
	protected Number minus(Object first, Object second) {
		if (type.equals("Integer"))
			return (Integer) first - (Integer) second;
		else if (type.equals("Double"))
			return (Double) first - (Double) second;
		else if (type.equals("Long"))
			return (Long) first - (Long) second;
		else if (type.equals("Float"))
			return (Float) first - (Float) second;
		else if (type.equals("Short"))
			return (Short) first - (Short) second;
		else // if (type.equals("Byte"))
			return (Byte) first - (Byte) second;
	}

	/**
	 * Computes the product of two numerical values. 
	 * @param first the first numerical value.
	 * @param second the second numerical value.
	 * @return the product of the two values.
	 */
	protected Number times(Object first, Object second) {
		if (type.equals("Integer"))
			return (Integer) first * (Integer) second;
		else if (type.equals("Double"))
			return (Double) first * (Double) second;
		else if (type.equals("Long"))
			return (Long) first * (Long) second;
		else if (type.equals("Float"))
			return (Float) first * (Float) second;
		else if (type.equals("Short"))
			return (Short) first * (Short) second;
		else // if (type.equals("Byte"))
			return (Byte) first * (Byte) second;
	}

	/**
	 * Computes the quotient of two numerical values. 
	 * @param first the first numerical value.
	 * @param second the second numerical value.
	 * @return the quotient of the first divided by the second value.
	 */
	protected Number divide(Object first, Object second) {
		if (type.equals("Integer"))
			return (Integer) first / (Integer) second;
		else if (type.equals("Double"))
			return (Double) first / (Double) second;
		else if (type.equals("Long"))
			return (Long) first / (Long) second;
		else if (type.equals("Float"))
			return (Float) first / (Float) second;
		else if (type.equals("Short"))
			return (Short) first / (Short) second;
		else // if (type.equals("Byte"))
			return (Byte) first / (Byte) second;
	}

	/**
	 * Computes the modulo of two numerical values. 
	 * @param first the first numerical value.
	 * @param second the second numerical value.
	 * @return the remainder of the first modulo the second value.
	 */
	protected Number modulo(Object first, Object second) {
		if (type.equals("Integer"))
			return (Integer) first % (Integer) second;
		else if (type.equals("Double"))
			return (Double) first % (Double) second;
		else if (type.equals("Long"))
			return (Long) first % (Long) second;
		else if (type.equals("Float"))
			return (Float) first % (Float) second;
		else if (type.equals("Short"))
			return (Short) first % (Short) second;
		else // if (type.equals("Byte"))
			return (Byte) first % (Byte) second;
	}

}
