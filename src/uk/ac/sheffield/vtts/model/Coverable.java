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
 * Coverable is the abstract ancestor of coverable parts of a specification.
 * Coverable is the ancestor of State and Transition, both of which may be
 * covered by generated Languages.  Coverable provides methods to record when
 * States and Transitions were covered, or not covered.  After a Machine has
 * been simulated, the covered flag will be set to true in Coverable elements 
 * that were covered.  After a Machine has been cleared, all covered flags
 * will be reset to false.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public abstract class Coverable extends Annotated {
	
	/**
	 * Volatile flag indicating whether this Coverable has been covered by a 
	 * given generated Language.  This attribute is not serialised.
	 */
	protected boolean covered = false;

	/**
	 * Creates a default unnamed Coverable.
	 */
	public Coverable() {
	}

	/**
	 * Creates a named Coverable.
	 * @param name the name of this Coverable.
	 */
	public Coverable(String name) {
		super(name);
	}
	
	/**
	 * Reports whether this Coverable has been covered, after simulating a 
	 * Machine with a given Language.
	 * @return true, if this Coverable was covered.
	 */
	public boolean isCovered() {
		return covered;
	}
	
	/**
	 * Sets whether this Coverable has been covered.  When a State or a 
	 * Transition has been covered, this method is used to set the covered
	 * flag to true.  When the Machine is cleared of such marks, this method
	 * is used to set the flag to false.
	 * @param value true or false.
	 * @return this Covered.
	 */
	public Coverable setCovered(boolean value) {
		covered = value;
		return this;
	}

}
