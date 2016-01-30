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
import java.util.Collections;
import java.util.List;


/**
 * Proposition represents a logical expression with a Boolean operator root
 * node.  The operands are expected to be other Predicates, or Boolean valued
 * Parameters.  There are five possible Boolean operators, whose names 
 * include:  or, and, not, implies and equals, although the last two are 
 * rarely used.  Two of the operators (and, or) are designed to accept two
 * or more operands (to reduce nesting and associativity issues), two further
 * operators (implies, equals) expect exactly two operands, and one operator
 * (not) expects a single operand.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Proposition extends Predicate {
	
	/**
	 * Validates the name of a logical Proposition and sets the expected
	 * number of operands.  All Boolean operators expect two operands,
	 * apart from "not", which expects one operand.
	 */
	protected void nameCheck() {
		String legalNames = "not,or,and,implies,equals,";
		if (! legalNames.contains(name + ','))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("not"))
			maxOperands = 1;
		else if (legalNames.substring(11).contains(name))
			maxOperands = 2;
		// else we don't set a limit on the operands
	}
	
	/**
	 * Checks the types of the operands of this Proposition predicate.
	 * The operands and the result must all have the type Boolean.
	 */
	protected void typeCheck() {
		if (! getType().equals("Boolean"))
			semanticError("has an illegal result type '" + type + "'.");
		for (Expression operand : getExpressions()) {
			String opType = operand.getType();
			if (! opType.equals("Boolean"))
				semanticError(name + " has an illegal operand type '" 
						+ opType + "'.");
		}
	}
	
	/**
	 * Creates a default logical Proposition.  The result type is automatically
	 * set to "Boolean".
	 */
	public Proposition() {
	}
	
	/**
	 * Creates a logical Proposition with the given name.  Checks that the name
	 * is one of the legal names for a logical Proposition.  The result type is
	 * automatically set to "Boolean".
	 * @param name the name of this Proposition.
	 */
	public Proposition(String name) {
		super(name);
	}
	
	/**
	 * Returns one of the operand Predicates at an index.  Converts the
	 * Expression to a Predicate, to enable recursive Predicate processing.
	 * @param index the index.
	 * @return the indexed operand, as a Predicate.
	 */
	public Predicate operand(int index) {
		Expression expr = expressions.get(index);
		if (expr instanceof Predicate)
			return (Predicate) expr;
		else if (expr instanceof Parameter)
			return new Atomic((Parameter) expr);
		else {
			semanticError("has an illegal operand: " + expr);
			return null;
		}
	}
	
	/**
	 * Returns the list of operand Predicates governed by this Proposition.
	 * Converts the Expression operands of this Proposition into a list of
	 * Predicates.  Each operand is either already some kind of Predicate,
	 * or it is some kind of Parameter, in which case it is wrapped as an
	 * Atomic predicate.  This method is used when treating Boolean-valued
	 * expressions uniformly as predicates.
	 * @return the list of operands as a list of Predicates.
	 */
	public List<Predicate> getPredicates() {
		List<Predicate> result = new ArrayList<Predicate>();
		for (Expression expr : expressions) {
			if (expr instanceof Predicate)
				result.add((Predicate) expr);
			else if (expr instanceof Parameter)
				result.add(new Atomic((Parameter) expr));
			else
				semanticError("contains a non-predicate: " + expr);
		}
		return result;
	}
	
	/**
	 * Converts this Proposition into a term dominated by AND, OR or NOT.
	 * Eliminates the operators "implies" and "equals" by logical conversion.
	 * @return the normalised form of this Proposition.
	 */
	public Proposition normalise() {
		if (name.equals("implies")) {
			Proposition result = new Proposition("or");
			result.addExpression(operand(0).negate().normalise());
			result.addExpression(operand(1).normalise());
			return result;
		}
		else if (name.equals("equals")) {
			Proposition result = new Proposition("and");
			Proposition lhs = new Proposition("implies");
			Proposition rhs = new Proposition("implies");
			lhs.addExpression(operand(0)).addExpression(operand(1));
			rhs.addExpression(operand(1)).addExpression(operand(0));
			result.addExpression(lhs.normalise());
			result.addExpression(rhs.normalise());
			return result;
		}
		else {
			Proposition result = new Proposition(name);
			for (Predicate predicate : getPredicates()) {
				result.addExpression(predicate.normalise());
			}
			return result;
		}
	}
	
	/**
	 * Reports whether this Proposition is consistent.  Chiefly checks large
	 * conjunctions to determine whether their conjuncts are satisfiable with
	 * any value assignments.  Disjunctions are checked to see if any disjunct
	 * is satisfiable.  Negations are checked to see if their single sub-term
	 * is satisfiable.  Other logical connectives are normalised first.
	 * @return true if this Predicate is consistent.
	 */
	public boolean isConsistent() {
		String name = getName();
		if (name.equals("and")) {
			return satisfyAll(getPredicates());
		}
		else if (name.equals("or")) {
			for (Predicate disjunct : getPredicates()) {
				if (disjunct.isConsistent())
					return true;
			}
			return false;
		}
		else if (name.equals("not"))
			return operand(0).isConsistent();
		else
			return normalise().isConsistent();
	}
		
	/**
	 * Checks the consistency of a set of conjuncts.  Determines whether it
	 * is possible to bind the ground variables in a set of conjuncts to any
	 * values, for which the conjuncts are simultaneously satisfiable.  Does
	 * this by computing all permutations of the conjuncts, then attempting
	 * to bind the ground variables in each permutation, until at least one 
	 * permutation succeeds in being bound, and is also satisfiable.  If no
	 * permutations can be satisfied, returns false.  If binding fails for
	 * any reason, returns true (the fail-safe assumption).
	 * @param predicates the list of conjuncts.
	 * @return true, if the conjuncts are simultaneously satisfiable.
	 */
	private boolean satisfyAll(List<Predicate> predicates) {
		//DEBUG
		//System.out.println("Inside satisfyAll(predicates) ...");
		//System.out.println("   with: " + this);
		List<List<Predicate>> permutations = permute(predicates);
		for (List<Predicate> arrangement : permutations) {
			boolean succeed = true;  // until proven false
			for (Predicate predicate : arrangement) {
				try {
					//DEBUG
					//System.out.println("      binding: " + predicate);
					predicate.rebind();  			// try to rebind
					succeed = predicate.evaluate(); // try to evaluate
					//DEBUG
					//System.out.print(succeed?"      succeeded":"      failed");
					//for (Expression expr : predicate.getExpressions()) {
					//	System.out.print(", " + expr.evaluate());
					//}
					//System.out.println();
				}
				catch (NullPointerException ex) {
					// FAILSAFE - handle null returns from map lookups.
					break;  // assume pred consistent, in the worst-case.
				}
				if (! succeed) {
					break;  // Forget rest of preds; this pred = false.
				}
			}
			for (Predicate predicate : arrangement) {
				predicate.unbind();  // CLEANUP - unbind all parameters.
			}
			if (succeed) {
				//DEBUG
				//System.out.println("   consistent!");
				return true;  // Forget rest of perms; this perm = true.
			}
		}
		//DEBUG
		//System.out.println("   not consistent!");
		return false;  // Bound every perm; none were satisfiable.
	}
					
	/**
	 * Computes all permutations of a list of Predicates.  The output is a
	 * list of lists, in which the Predicates from the input list are arranged
	 * in all possible orders.  This is used when attempting to bind a set of
	 * Predicates, in which the order of binding may result in different value
	 * assignments.  Notionally, at least one order of binding should succeed,
	 * if the Predicates are simultaneously satisfiable.
	 * @param input a list of Predicates.
	 * @return all permutations of this list.
	 */
	private List<List<Predicate>> permute(List<Predicate> input) {
		List<List<Predicate>> output = new ArrayList<List<Predicate>>();
		if (input.isEmpty()) {
			output.add(Collections.<Predicate> emptyList());
			return output;
		}
		else {
			for (Predicate predicate : input) {
				List<Predicate> rest = new ArrayList<Predicate>(input);
				rest.remove(predicate);
				for (List<Predicate> oldList : permute(rest)) {
					List<Predicate> newList = new ArrayList<Predicate>(oldList);
					newList.add(predicate);
					output.add(newList);
				}
			}
		}
		return output;
	}
	
	/**
	 * Tests whether this Proposition subsumes another Predicate.  Checks 
	 * whether this Proposition is in normal form, dominated by AND, OR, or
	 * NOT; otherwise first converts this Proposition, which is dominated by
	 * IMPLIES or EQUALS, to normal form, before recursively checking for
	 * subsumption.  If the other Predicate is also a Proposition, dispatches
	 * to a subroutine dealing with this special case.  If this Proposition
	 * is dominated by AND, checks whether all of its conjuncts subsume the
	 * other.  If it is dominated by OR, checks whether one of its disjuncts
	 * subsumes the other.  Otherwise, this is dominated by NOT; so reverses
	 * polarity and checks whether the negation of the other subsumes the 
	 * negation of this.  Returns false if this would lead to infinite
	 * regression.
	 * @return true, if this Proposition subsumes the other Predicate.
	 */
	public boolean subsumes(Predicate other) {
		String thisName = getName();
		if (thisName.equals("equals") || thisName.equals("implies"))
			return normalise().subsumes(other);
		else if (other instanceof Proposition)
			return subsumesProposition((Proposition) other);
		else if (thisName.equals("and"))
			return allConjunctsSubsume(other);
		else if (thisName.equals("or"))
			return oneDisjunctSubsumes(other);
		else if (thisName.equals("not")) {
			Predicate negatedOther = other.negate();
			if (negatedOther.getName().equals("not"))
				return false;  // halt infinite regression
			else  // reverse polarity
				return negatedOther.subsumes(negate());
		}
		else
			return other.isFalseConstant();  // Degenerate case.
	}
	
	/**
	 * Tests whether one of this OR-Proposition's disjuncts subsumes the 
	 * other Predicate.  The other Predicate is not a compound Proposition,
	 * but an atomic Comparison or Membership predicate.  
	 * @param other the other Predicate.
	 * @return true if any of this Proposition's disjuncts subsumes the 
	 * other.
	 */
	protected boolean oneDisjunctSubsumes(Predicate other) {
		for (Predicate disjunct : getPredicates()) {
			if (disjunct.subsumes(other))
				return true;
		}
		return false;
	}
	
	/**
	 * Tests whether all of this AND-Proposition's conjuncts subsume the
	 * other Predicate.  The other Predicate is not a compound Proposition,
	 * but an atomic Comparison or Membership predicate.
	 * @param other the other Predicate.
	 * @return true if all of this Proposition's conjuncts subsume the
	 * other.
	 */
	protected boolean allConjunctsSubsume(Predicate other) {
		for (Predicate conjunct : getPredicates()) {
			if (! conjunct.subsumes(other))
				return false;
		}
		return true;
	}
	
	/**
	 * Tests whether this Proposition subsumes one conjunct of the other
	 * AND-Proposition.  If this is also an AND-Proposition, all of this
	 * Proposition's conjuncts must subsume at least one of the other
	 * Proposition's conjuncts.  If this is an OR-Proposition, at least
	 * one of this Proposition's disjuncts must subsume at least one of
	 * the other Proposition's conjuncts.
	 * Special version for Propositions, overrides version in Predicate.
	 */
	@Override
	protected boolean subsumesOneConjunctOf(Proposition other) {
		String thisName = getName();
		if (thisName.equals("and")) {
			// All of this AND-Proposition's conjuncts must subsume at least
			// one of the other AND-Proposition's conjuncts.
			for (Predicate conjunct : getPredicates()) {
				if (! conjunct.subsumesOneConjunctOf(other))
					return false;
			}
			return true;
		}
		else if (thisName.equals("or")) {
			// At least one of this OR-Proposition's disjuncts must subsume
			// at least one of the other AND-Proposition's conjuncts.
			for (Predicate disjunct : getPredicates()) {
				if (disjunct.subsumesOneConjunctOf(other)) 
					return true;
			}
			return false;
		}
		else  // thisName.equals("not")
			return super.subsumesOneConjunctOf(other);
	}
	
	/**
	 * Tests whether this Proposition subsumes all disjuncts of the other
	 * OR-Proposition.  If this is also an OR-Proposition, some of this 
	 * Proposition's disjuncts must subsume all of the other's disjuncts.
	 * If this is an AND-Proposition, all of this Proposition's conjuncts
	 * must subsume all of the other's disjuncts.
	 * Special version for Propositions, overrides version in Predicate.
	 */
	@Override
	protected boolean subsumesAllDisjunctsOf(Proposition other) {
		String thisName = getName();
		if (thisName.equals("or")) {
			// At least one of this OR-Proposition's disjuncts must subsume
			// all of the other OR-Proposition's disjuncts.
			for (Predicate disjunct : other.getPredicates()) {
				if (! oneDisjunctSubsumes(disjunct))
					return false;
			}
			return true;
		}
		else if (thisName.equals("and")) {
			// All of this AND-Proposition's conjuncts must subsume all of 
			// the other OR-Proposition's disjuncts.
			for (Predicate disjunct : other.getPredicates()) {
				if (! allConjunctsSubsume(disjunct))
					return false;
			}
			return true;
		}
		else 
			return super.subsumesAllDisjunctsOf(other);
	}
	
	/**
	 * Returns the complement of this logical Proposition.  The complement of
	 * NOT(X) is X; the complement of AND(X, Y) is OR(NOT(X), NOT(Y)).  The
	 * complement of OR(X, Y) is AND(NOT(X), NOT(Y)).  The complement of
	 * IMPLIES(X, Y) is determined by first normalising this to OR(NOT(X), Y)
	 * subsequently negating to yield AND(X, NOT(Y)).  The complement of
	 * EQUALS(X, Y) is determined by first normalising this to AND(OR(NOT(X),
	 * Y), OR(NOT(Y), X)) and subsequently negating to yield OR(AND(X, 
	 * NOT(Y)), AND(Y, NOT(X))).
	 * @return the negation, or complement of this logical Proposition.
	 */
	public Predicate negate() {
		String thisName = getName();
		if (thisName.equals("equals") || thisName.equals("implies"))
			return normalise().negate();
		else {
			Predicate result = null;
			if (thisName.equals("and")) {
				result = new Proposition("or");
				for (Predicate predicate : getPredicates())
					result.addExpression(predicate.negate());
			}
			else if (thisName.equals("or")) {
				result = new Proposition("and");
				for (Predicate predicate : getPredicates())
					result.addExpression(predicate.negate());				
			}
			else  // thisName.equals("not")
				result = operand(0);
			return result;
		}
	}
		
	/**
	 * Returns a list of refinements of this Proposition.  If this Proposition
	 * is a logical-AND or logical-OR, we want to collect the atomic Predicates
	 * under this Proposition.  Refines each of the operands, and returns a 
	 * list of all of these refinements.  If this Proposition is logical-NOT, 
	 * collects the negation of each of the refinements of this Proposition's
	 * (negated) operand.
	 */
	public List<Predicate> refine() {
		List<Predicate> result = new ArrayList<Predicate>();
		if (name.equals("not")) {
			for (Predicate refined : operand(0).refine()) {
				Proposition proposition = new Proposition(name);
				proposition.addExpression(refined);
				result.add(proposition);
			}
		}
		else {
			for (Predicate operand : getPredicates()) {
				result.addAll(operand.refine());
			}
		}
		return result;
	}

	/**
	 * Executes this Proposition on its operands.  Expects one operand for a
	 * negation, two operands for implies or equals, and two or more operands
	 * otherwise (for and, or).  Branches according to the name of the
	 * operation, and evaluates operands AND, OR lazily.
	 */
	public Boolean evaluate() {
		typeCheck();
		if (name.equals("not")) {
			return ! operand(0).evaluate();
		}
		else if (name.equals("and")) {
			for (Predicate predicate : getPredicates()) {
				if (! predicate.evaluate())
					return false;
			}
			return true;
		}
		else if (name.equals("or")) {
			for (Predicate predicate : getPredicates()) {
				if (predicate.evaluate())
					return true;
			}
			return false;
		}
		else {
			boolean val0 = operand(0).evaluate();
			boolean val1 = operand(1).evaluate();
			if (name.equals("equals"))
				return val0 == val1;
			else  // name.equals("implies")
				return !val0 || val1;
		}
	}

	/**
	 * Rebinds the unbound operands of this Proposition, so that it yields 
	 * true.  This is a heuristic approach that works most of the time, but
	 * not universally.  For example: if operand[0] is already bound to 
	 * false, no binding of operand[1] will make AND true.
	 */
	@Override
	protected void rebindTrue() {
		if (! operand(0).isBound()) {
			if (name.equals("not"))
				operand(0).rebind((Boolean) false);
			else {
				if (! operand(1).isBound())
					operand(1).rebind((Boolean) true);
				Boolean value1 = operand(1).evaluate();
				if (name.equals("implies") || name.equals("equals"))
					operand(0).rebind(value1);
				else
					for (Expression operand : expressions) {
						if (! operand.isBound())
							operand.rebind((Boolean) true);
					}
			}
		}
		else if (maxOperands != 1 && ! operand(1).isBound()) {
			Boolean value0 = operand(0).evaluate();
			if (name.equals("implies") || name.equals("equals"))
				operand(1).rebind(value0);
			else
				for (Expression operand : expressions) {
					if (! operand.isBound())
						operand.rebind((Boolean) true);
				}
		}
	}

	/**
	 * Rebinds the unbound operands of this Proposition, so that it yields
	 * false.  This is a heuristic approach that works most of the time, but
	 * not universally.  For example: if operand[0] is already bound to 
	 * false, no binding of operand[1] will make IMPLIES false.
	 */
	@Override
	protected void rebindFalse() {
		if (! operand(0).isBound()) {
			if (name.equals("not"))
				operand(0).rebind((Boolean) true);
			else {
				if (! operand(1).isBound())
					operand(1).rebind((Boolean) false);
				Boolean value1 = (Boolean) operand(1).evaluate();
				if (name.equals("implies") || name.equals("equals"))
					operand(0).rebind((Boolean)(value1 ? false : true));
				else
					for (Expression operand : expressions) {
						if (! operand.isBound())
							operand.rebind((Boolean) false);
					}
			}
		}
		else if (maxOperands != 1 && ! operand(1).isBound()) {
			Boolean value0 = (Boolean) operand(0).evaluate();
			if (name.equals("implies") || name.equals("equals"))
				operand(1).rebind((Boolean)(value0 ? false : true));		
			for (Expression operand : expressions) {
				if (! operand.isBound())
					operand.rebind((Boolean) false);
			}
		}
	}

}
