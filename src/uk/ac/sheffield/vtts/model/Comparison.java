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
import java.util.List;


/**
 * Comparison represents an inequality expression, comparing two operands.
 * A Comparison is a kind of boolean expression that directly compares its 
 * two operands.  There are six comparison operations, whose names include:
 * equals, notEquals, lessThan, moreThan, notLessThan, and notMoreThan.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Comparison extends Predicate {
	
	/**
	 * The value type of this Comparison's argument operands.  Set when types
	 * are checked, and used when binding values to unbound operands.
	 */
	private String valueType = null;
	
	/**
	 * Checks the name of a Comparison predicate and sets the expected
	 * number of operands.  All Comparisons expect two operands.
	 */
	protected void nameCheck() {
		String legalNames = 
				"equals,notEquals,lessThan,moreThan,notLessThan,notMoreThan,";
		if (! legalNames.contains(name +','))
			semanticError("has an illegal operator name '" + name + "'.");
		maxOperands = 2;
	}

	/**
	 * Checks that the operand types are consistent, and the result type is
	 * Boolean.  Caches the operand valueType, which is used as a dispatching
	 * token during evaluation. 
	 */
	protected void typeCheck() {
		if (! getType().equals("Boolean"))
			semanticError("has an illegal result type '" + type + "'.");
		for (Expression operand : getExpressions()) {
			String opType = operand.getType();
			if (valueType != null && !opType.equals(valueType))
				semanticError("has operands of conflicting types '"
						+ valueType + ", " + opType + "'.");
			valueType = opType;
		}
	}
	
	/**
	 * Creates a default Comparison.  The result type is automatically set
	 * to "Boolean".
	 */
	public Comparison() {
	}
	
	/**
	 * Creates a Comparison with the given name.  Checks that the name is
	 * one of the legal names for a Comparison.  The result type is
	 * automatically set to "Boolean".
	 * @param name the name of this Comparison.
	 */
	public Comparison(String name) {
		super(name);
	}
	
	/**
	 * Tests whether this Comparison subsumes the other Predicate.  If
	 * the other Predicate is also a Comparison, tests whether this is a 
	 * broader Comparison than the other.  If the other Predicate is an
	 * AND-Proposition, tests whether this Comparison subsumes one of the
	 * other's conjuncts.  Otherwise, returns false.
	 * @param other the other Predicate.
	 * @return true if this Comparison subsumes the other Predicate.
	 */
	public boolean subsumes(Predicate other) {
		if (other instanceof Comparison) {
			Comparison comparison = (Comparison) other;
			return subsumesComparison(comparison) || 
					subsumesComparison(comparison.reverse());
		}
		else if (other instanceof Proposition)
			return subsumesProposition((Proposition) other);
		else
			return other.isFalseConstant();  // Degenerate case.
	}
	
	/**
	 * Tests whether this comparison subsumes the other comparison.  If
	 * the two conditions test the same operands, then if this condition
	 * is identical to, or broader than, the other condition, returns true;
	 * otherwise returns false.
	 * @param other the other comparison.
	 * @return true if this comparison subsumes the other one.
	 */
	protected boolean subsumesComparison(Predicate other) {
		if (getExpressions().equals(other.getExpressions())) {
			if (name.equals(other.name))
				return true;
			else if (name.equals("notEquals"))
				return (other.name.equals("lessThan") ||
						other.name.equals("moreThan"));
			else if (name.equals("notMoreThan"))
				return (other.name.equals("equals") ||
						other.name.equals("lessThan"));
			else if (name.equals("notLessThan"))
				return (other.name.equals("equals") ||
						other.name.equals("moreThan"));
			else
				return false;
		}
		else
			return false;
	}

	/**
	 * Reports whether this Comparison is consistent.  Returns false in the
	 * two cases of: lessThan(x, bottom) and moreThan(bottom, y).  Otherwise
	 * returns true.
	 * @return true if this Comparison is consistent.
	 */
	@Override
	public boolean isConsistent() {
		String name = getName();
		if (name.equals("lessThan"))
			return ! operand(1).isBottom();
		else if (name.equals("moreThan"))
			return ! operand(0).isBottom();
		else
			return true;
	}
	
	/**
	 * Returns the complement of this Comparison.  Negates the inequality
	 * relationship, but keeps the same operands.
	 */
	public Predicate negate() {
		Predicate result = new Comparison(getNegatedName());
		for (Expression operand : getExpressions()) {
			result.addExpression(operand);
		}
		return result;
	}
	
	/**
	 * Algorithm for returning the negated name of this Comparison.  Used
	 * when constructing the complement of this Comparison.
	 * @return the negated name.
	 */
	private String getNegatedName() {
		String name = getName();
		if (name.startsWith("not")) {
			if (name.equals("notEquals"))
				return "equals";
			else if (name.equals("notLessThan"))
				return "lessThan";
			else // "notMoreThan"
				return "moreThan";
		}
		else {
			if (name.equals("equals"))
				return "notEquals";
			else if (name.equals("lessThan"))
				return "notLessThan";
			else // "moreThan"
				return "notMoreThan";
		}
	}

	/**
	 * Returns the symmetric reversal of this Comparison.  Reverses the 
	 * inequality relationship and reverses the order of operands.
	 * @return a symmetric Comparison expressing the same logical 
	 * relationship, but with the order of operands reversed.
	 */
	public Predicate reverse() {
		Predicate result = new Comparison(getReversedName());
		result.addExpression(operand(1)).addExpression(operand(0));
		return result;
	}
	
	/**
	 * Algorithm for returning the reversed name of this Comparison.  Used
	 * when constructing the symmetrically reversed Comparison.
	 * @return the reversed name.
	 */
	private String getReversedName() {
		String name = getName();
		if (name.startsWith("not")) {
			if (name.equals("notLessThan"))
				return "notMoreThan";
			else if (name.equals("notMoreThan"))
				return "notLessThan";
			else // "notEquals"
				return name;
		}
		else {
			if (name.equals("lessThan"))
				return "moreThan";
			else if (name.equals("moreThan"))
				return "lessThan";
			else // "equals"
				return name;
		}
	}
	
	/**
	 * Returns a list of refinements of this Comparison.  If this Comparison
	 * can be broken down into more atomic Comparisons, breaks it down into
	 * its atomic Comparisons, returning these as a list.  Otherwise, returns
	 * this Comparison as a singleton list.
	 * @return the refinements of this Comparison.
	 */
	public List<Predicate> refine() {
		if (name.startsWith("not") && operand(0).isOrdered()) {
			Predicate refine1 = new Comparison();
			Predicate refine2 = new Comparison();
			List<Predicate> result = new ArrayList<Predicate>();
			if (name.equals("notEquals")) {
				refine1.setName("lessThan");
				refine2.setName("moreThan");
			}
			else if (name.equals("notMoreThan")) {
				refine1.setName("equals");
				refine2.setName("lessThan");
			}
			else {  // name.equals("notLessThan")
				refine1.setName("equals");
				refine2.setName("moreThan");
			}
			for (Expression operand : expressions) {
				refine1.addExpression(operand);
				refine2.addExpression(operand);
			}
			result.add(refine1);
			result.add(refine2);
			return result;
		}
		else 
			return super.refine();
	}

	/**
	 * Executes this Comparison predicate on its operands.  Checks the 
	 * consistency of the operand types and then branches according to 
	 * the name of this Comparison predicate.
	 * @return true if the predicate holds between the operands.
	 */
	public Boolean evaluate() throws SemanticError {
		typeCheck();  // Caches the valueType.
		Object value0 = operand(0).evaluate();
		Object value1 = operand(1).evaluate();
		if (name.equals("equals"))
			return equals(value0, value1);
		else if (name.equals("notEquals"))
			return notEquals(value0, value1);
		else if (name.equals("lessThan"))
			return lessThan(value0, value1);
		else if (name.equals("moreThan"))
			return moreThan(value0, value1);
		else if (name.equals("notLessThan"))
			return notLessThan(value0, value1);
		else // if (name.equals("notMoreThan"))
			return notMoreThan(value0, value1);
	}

	/**
	 * Reports whether two values are equal.  Uses Java's built-in equals()
	 * method to judge the equality of two values of the same type.
	 * @param first the first value.
	 * @param second the second value.
	 * @return true, if the values are equal.
	 */
	protected Boolean equals(Object first, Object second) {
		return first.equals(second);
	}

	/**
	 * Reports whether two values are not equal.  Returns the logical negation
	 * of the comparison: equals(first, second).
	 * @param first the first value.
	 * @param second the second value.
	 * @return true, if the values are not equal.
	 */
	protected Boolean notEquals(Object first, Object second) {
		return ! first.equals(second);
	}

	/**
	 * Reports whether the first value is less than the second value.  This
	 * is determined according to the value-type.  String and Character values
	 * are compared lexicographically; Number values are compared by magnitude
	 * using Java's built-in compareTo(first, second).
	 * @param first the first value.
	 * @param second the second value.
	 * @return true, if the first value is less than the second value.
	 */
	protected Boolean lessThan(Object first, Object second) {
		if (valueType.equals("String"))
			return ((String) first).compareTo((String) second) < 0;
		else if (valueType.equals("Integer"))
			return ((Integer) first).compareTo((Integer) second) < 0;
		else if (valueType.equals("Double"))
			return ((Double) first).compareTo((Double) second) < 0;
		else if (valueType.equals("Long"))
			return ((Long) first).compareTo((Long) second) < 0;
		else if (valueType.equals("Float"))
			return ((Float) first).compareTo((Float) second) < 0;
		else if (valueType.equals("Short"))
			return ((Short) first).compareTo((Short) second) < 0;
		else if (valueType.equals("Byte"))
			return ((Byte) first).compareTo((Byte) second) < 0;
		else if (valueType.equals("Character"))
			return ((Character) first).compareTo((Character) second) < 0;
		else {
			semanticError("values are of an unordered type: '" + 
					valueType + "'.");
			return null;
		}			
	}

	/**
	 * Reports whether the first value is greater than the second value.
	 * Returns the result of the reverse comparison:  lessThan(second, first).
	 * @param first the first value.
	 * @param second the second value.
	 * @return true, if the first value is greater than the second value.
	 */
	protected Boolean moreThan(Object first, Object second) {
		return lessThan(second, first);
	}

	/**
	 * Reports whether the first value is greater than, or equal to the second
	 * value.  Returns the result of the disjunction:  equals(first, second) OR
	 * lessThan(second, first).
	 * @param first the first value.
	 * @param second the second value.
	 * @return true, if the first value is not less than the second value.
	 */
	protected Boolean notLessThan(Object first, Object second) {
		return equals(first, second) || lessThan(second, first);
	}

	/**
	 * Reports whether the first value is less than, or equal to the second 
	 * value.  Returns the result of the disjunction:  equals(first, second) OR
	 * lessThan(first, second).
	 * @param first the first value.
	 * @param second the second value.
	 * @return true, if the first value is not greater than the second value.
	 */
	protected Boolean notMoreThan(Object first, Object second) {
		return equals(first, second) || lessThan(first, second);
	}

	/**
	 * Rebinds the unbound operands of this Comparison, so that it yields 
	 * true.  If either operand is unbound, binds it to a value satisfying
	 * the constraint with respect to the other operand.  If both are
	 * unbound, first binds the second operand to a default value.
	 */
	protected void rebindTrue() {
		if (! operand(0).isBound()) {
			if (! operand(1).isBound())
				operand(1).rebind();  // to any default value
			Object value1 = operand(1).evaluate();
			if (name.equals("lessThan"))
				operand(0).rebind(factory.getPredecessor(value1, valueType));
			else if (name.equals("notEquals") || name.equals("moreThan"))
				operand(0).rebind(factory.getSuccessor(value1, valueType));
			else // if name.equals("equals" || "notLessThan" || "notMoreThan")
				operand(0).rebind(value1);
		}
		else if (! operand(1).isBound()) {
			// operand(0) is already bound
			Object value0 = operand(0).evaluate();
			if (name.equals("moreThan"))
				operand(1).rebind(factory.getPredecessor(value0, valueType));
			else if (name.equals("notEquals") || name.equals("lessThan"))
				operand(1).rebind(factory.getSuccessor(value0, valueType));
			else // if name.equals("equals" || "notLessThan" || "notMoreThan")
				operand(1).rebind(value0);	
		}
	}
	
	/**
	 * Rebinds the unbound operands of this Comparison, so that it yields 
	 * false.  If either operand is unbound, binds it to a value violating
	 * the constraint with respect to the other operand.  If both are
	 * unbound, first binds the second operand to a default value.
	 */
	protected void rebindFalse() {
		if (! operand(0).isBound()) {
			if (! operand(1).isBound())
				operand(1).rebind();  // to any default value
			Object value1 = operand(1).evaluate();
			if (name.equals("notLessThan"))
				operand(0).rebind(factory.getPredecessor(value1, valueType));
			else if (name.equals("equals") || name.equals("notMoreThan"))
				operand(0).rebind(factory.getSuccessor(value1, valueType));
			else // if name.equals("lessThan" || "moreThan" || "notEquals")
				operand(0).rebind(value1);
		}
		else if (! operand(1).isBound()) {
			// operand(0) is already bound
			Object value0 = operand(0).evaluate();
			if (name.equals("notLessThan"))
				operand(1).rebind(factory.getSuccessor(value0, valueType));
			else if (name.equals("equals") || name.equals("notMoreThan"))
				operand(1).rebind(factory.getPredecessor(value0, valueType));
			else // if name.equals("equals" || "notLessThan" || "notMoreThan")
				operand(1).rebind(value0);			
		}		
	}
	
}

