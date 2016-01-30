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
 * Atomic is a degenerate predicate representing an atomic Boolean value.
 * Atomic is a special adapter that allows a Parameter to be treated like a
 * Predicate, when testing for logical subsumption.  Atomic objects are only
 * created internally by the verification tool, and are not intended for end
 * user models, which instead should use Parameters (Input, Output, Variable,
 * Constant) with Boolean types directly.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Atomic extends Predicate {

	/**
	 * Checks the name of an Atomic predicate and sets the number of 
	 * operands.  There is always only a single operand.  The name of this
	 * Atomic predicate is the same as the name of the wrapped operand.
	 */
	@Override
	protected void nameCheck() throws SemanticError {
		if (! name.equals(operand(0).getName()))
			semanticError("has an illegal name " + name + "'.");
		maxOperands = 1;
	}

	/**
	 * Checks the type of this Atomic predicate.  
	 */
	@Override
	protected void typeCheck() throws SemanticError {
		if (! getType().equals("Boolean"))
			semanticError("has an invalid type '" +	type + "'.");
		String opType = operand(0).getType();
		if (! opType.equals("Boolean"))
			semanticError("has an invalid type '" +	opType + 
					"' for its wrapped parameter '" + name + "'.");
	}

	/**
	 * Creates a default Atomic predicate.  The result type is automatically
	 * set to "Boolean".
	 */
	public Atomic() {
	}
	
	/**
	 * Creates an Atomic predicate by wrapping a Parameter.  The name of this
	 * Atomic predicate is taken from the name of the Parameter, and the type
	 * is automatically set to "Boolean".  Adds the Parameter as the wrapped
	 * sub-expression.
	 * @param parameter the parameter to wrap.
	 */
	public Atomic(Parameter parameter) {
		super(parameter.getName());
		addExpression(parameter);
	}
	
	/**
	 * Creates an Atomic predicate standing for a boolean constant value. 
	 * This constructor is used exclusively during negation of a constant
	 * Atomic predicate.
	 * @param value the constant true or false.
	 */
	public Atomic(boolean value) {
		super(String.valueOf(value));
		addExpression(new Constant(name, "Boolean").setContent(name));
	}
	
	/**
	 * Adds an expression as an operand to this Assignment expression.
	 * @param expression the operand expression.
	 * @return this functional expression.
	 */
	public Atomic addExpression(Expression expression) {
		if (expressions.isEmpty()) { 
			if (! (expression instanceof Parameter))
				semanticError("must have a Parameter as its operand.");
		}
		return (Atomic) super.addExpression(expression);
	}
	
	/**
	 * Negates this Atomic predicate.  If this Atomic predicate wraps a
	 * Constant, returns an Atomic predicate wrapping the complementary
	 * Constant.  Otherwise, constructs a Proposition negating the value
	 * of this Atomic predicate (which wraps a variable kind of Parameter).
	 */
	public Predicate negate() {
		if (operand(0) instanceof Constant) {
			if (operand(0).evaluate().equals(true))
				return new Atomic(false);
			else
				return new Atomic(true);
		}
		else
			return super.negate();
	}
	
	/**
	 * Returns true if this Atomic predicate wraps the boolean Constant true.
	 * @return true, if this represents the true constant.
	 */
	public boolean isTrueConstant() {
		if (operand(0) instanceof Constant)
			return operand(0).evaluate().equals(true);
		else
			return false;
	}
	
	/**
	 * Returns true if this Atomic predicate wraps the boolean Constant false.
	 * @return true, if this represents the false constant.
	 */
	public boolean isFalseConstant() {
		if (operand(0) instanceof Constant)
			return operand(0).evaluate().equals(false);
		else
			return false;
	}
	
	/**
	 * Evaluates this Atomic predicate.  Evaluates the wrapped Parameter, 
	 * which has a Boolean value.
	 */
	@Override
	public Boolean evaluate() {
		typeCheck();
		return (Boolean) operand(0).evaluate();
	}

	/**
	 * Tests whether this Atomic predicate subsumes the other Predicate.  The
	 * Constant "true" subsumes everything; whereas the Constant "false" only
	 * subsumes "false".  Any variable Parameter only subsumes the same-named
	 * parameter.  If the other is a Proposition, applies the subsumption
	 * rules for conjunction, disjunction and negation.  Otherwise, returns
	 * false.
	 * @return true, if this Atomic predicate subsumes the other predicate.
	 */
	@Override
	public boolean subsumes(Predicate other) {
		if (isTrueConstant())
			return true;
		else if (name.equals(other.getName()))  // Identical parameter names.
			return true;
		else if (other instanceof Proposition)
			return subsumesProposition((Proposition) other);
		else
			return other.isFalseConstant();  // Degenerate case.
	}
	
	/**
	 * Rebinds this Atomic predicate so that it yields true.  Propagates the
	 * request to the wrapped Parameter.
	 */
	@Override
	protected void rebindTrue() {
		operand(0).rebind((Boolean) true);
	}

	/**
	 * Rebinds this Atomic predicate so that it yields false.  Propagates the
	 * request to the wrapped Parameter.
	 */
	@Override
	protected void rebindFalse() {
		operand(0).rebind((Boolean) false);
	}
	
	/**
	 * Converts this Atomic predicate to a printable string.  The output is 
	 * a string of the form: varName, where this is the name of the wrapped
	 * Parameter.
	 */
	public String toString() {
		return operand(0).toString();
	}

}
