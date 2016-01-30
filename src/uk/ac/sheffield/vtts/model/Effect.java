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
 * Effect represents a particular set of value-modifications to parameters.
 * An Effect is a kind of Binding that occurs as a consequence of executing
 * a given Scenario in an Operation.  Whereas a Binding sets the initial
 * variable bindings, the Effect sets the consequential bindings.  A Effect
 * may bind an Output or a Variable, only.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Effect extends Binding {
	
	/**
	 * Creates a default Effect.
	 */
	public Effect() {
	}
	
	/**
	 * Adds an Assignment to this Effect.  Each Assignment in this Effect
	 * must bind a unique Parameter, unless the Parameter being bound is a
	 * temporary local Variable, which may be bound more than once.
	 * @param assignment the Assignment to add.
	 * @return this Effect.
	 */
	public Effect addAssignment(Assignment assignment) {
		Parameter param = (Parameter) assignment.operand(0);
		if (param instanceof Variable) {
			assignments.put(param.getName(), assignment);
			return this;
		}
		else
			return (Effect) super.addAssignment(assignment);
	}

}

