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

package uk.ac.sheffield.vtts.ground;

/**
 * Factory is the abstract interface satisfied by concrete object factories.
 * A Factory is a utility for synthesising run-time object instances from
 * the text description of their value and type.  The main functionality
 * provided by a Factory is the method createObject(), which creates any
 * kind of Object, given a description of its value and its type.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 *
 */
public interface Factory {
	
	/**
	 * Creates a strongly-typed instance of any kind of Java Object with the
	 * given printed representation and Java type.  Performs an unchecked 
	 * type cast on the result of createObject().  Provided for convenience.
	 * @param <T> the returned object type.
	 * @param value the value, as a String.
	 * @param type the Java type, as a String.
	 * @return a new instance of the type.
	 */
	public abstract <T> T create(String value, String type);
	
	/**
	 * Creates an instance of any type of Object, with the given value.  This
	 * is the principal factory method, but is abstract, to allow subclasses
	 * to decode type strings styled in different formats.
	 * @param value the value, as a String.
	 * @param type the type, as a String.
	 * @return a new instance of the type.
	 */
	public abstract Object createObject(String value, String type);

}
