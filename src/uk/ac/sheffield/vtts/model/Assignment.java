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
 * Assignment represents a single variable-binding expression.  Assignment is
 * both an initialisation operator and a re-assignment operator.  It can be
 * used to populate Input, Output and Variable parameters with their initial
 * value; and it can also be used to re-assign a new value to a Variable.
 * The Assignment operator name indicates whether the assigned value should
 * be exactly equal to a given value, more or less than a given value, or
 * not equal to a given value.  The valid operator names include: equals,
 * notEquals, moreThan, lessThan.  The latter two operators may also be
 * used with a single operand for unit increment or decrement operations.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Assignment extends Function {
	
	/**
	 * Checks the operator name of this Assignment expression and sets the
	 * maximum number of operands to two.  The legal operator names include
	 * "equals, notEquals, moreThan, lessThan".  The latter two may be used
	 * for unit increment and decrement with a single variable operand; the
	 * ModelFactory has a notion of what it means to increment or decrement
	 * a value of each model type.
	 */
	protected void nameCheck() {
		String legalNames = "equals,notEquals,moreThan,lessThan,";
		if (! legalNames.contains(name + ','))
			semanticError("has an illegal operator name '" + name + "'.");
		maxOperands = 2;
	}

	/**
	 * Checks that the operand types of this Assignment are consistent, and
	 * the result type is Void.  If this Assignment has one operand, then this
	 * could be any single type for an increment or decrement; otherwise both
	 * operands must be of the same type.
	 * @throws SemanticError if a type inconsistency is detected.
	 */
	@Override
	protected void typeCheck() throws SemanticError {
		if (! getType().equals("Void"))
			semanticError("has an illegal result type '" + getType() + "'.");
		if (expressions.size() > 1) {
			String type0 = operand(0).getType();
			String type1 = operand(1).getType();
			if (! type0.equals(type1)) {
				semanticError("has operands of conflicting types '"
						+ type0 + ", " + type1 + "'.");
			}
		}
		else {
			if (! (name.equals("lessThan") || name.equals("moreThan"))) {
				semanticError("assignment operator '" + name + 
						"' has too few operands.");
			}
		}
	}

	/**
	 * Creates a default Assignment.  Sets the type to "Void".
	 */
	public Assignment() {
		setType("Void");
	}
	
	/**
	 * Creates a named Assignment.  The operator name indicates what kind of
	 * assignment is intended, from: "equals, notEquals, moreThan, lessThan".
	 * Also sets the type to "Void".
	 * @param name the operator name.
	 */
	public Assignment(String name) {
		super(name, "Void");
	}
	
	/**
	 * Creates a named and typed Assignment.  The operator name indicates the
	 * kind of assignment, from: "equals, notEquals, moreThan, lessThan". 
	 * Always sets the type to "Void", no matter what type was supplied
	 * (fail-safe).
	 * @param name the assignment operator name.
	 * @param type the type of the assignment, "Void".
	 */
	public Assignment(String name, String type) {
		super(name, "Void");
	}
	
	/**
	 * Sets the type of this Assignment.  Always sets the type to "Void", no
	 * matter what type was supplied (fail-safe).
	 */
	@Override
	public Assignment setType(String type) {
		this.type = "Void";
		return this;
	}

	/**
	 * Adds an expression as an operand to this Assignment expression.
	 * @param expression the operand expression.
	 * @return this functional expression.
	 */
	public Assignment addExpression(Expression expression) {
		if (expressions.isEmpty()) { 
			if (! expression.isAssignable())
				semanticError("must have an assignable first operand.");
		}
		return (Assignment) super.addExpression(expression);
	}
	
	/**
	 * Executes this Assignment on its operands.  Expects the first operand
	 * to be an assignable Parameter.  Performs one of an assignment,  a unit
	 * increment, or unit decrement operation.  The notEquals operator is 
	 * treated as a unit increment.
	 */
	public Object evaluate() {
		typeCheck();
		Parameter parameter = (Parameter) operand(0);
		String paramType = parameter.getType();
		Object value = (expressions.size() > 1 ? operand(1).evaluate() :
			parameter.evaluate());
		if (name.equals("equals"))
			parameter.assign(value);
		else if (name.equals("lessThan"))
			parameter.assign(factory.getPredecessor(value, paramType));
		else   // name == "moreThan" || name == "notEquals"
			parameter.assign(factory.getSuccessor(value, paramType));
		return parameter;
	}
	
	/**
	 * Rebinds the unbound operands of this Assignment.  Degenerate version of
	 * this method, which does nothing.
	 */
	@Override
	public void rebind() {
	}

	/**
	 * Rebinds the unbound operands of this Assignment, so that it yields the
	 * given (null) result.  Degenerate version of this method, which does 
	 * nothing.  
	 */
	@Override
	public void rebind(Object result) {
	}
	
}
