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

import java.util.Map.Entry;

/**
 * SimpleEntry is a public class that models a Pair in the model language.
 * SimpleEntry is a minimal public extension of AbstractMap.SimpleEntry, 
 * which we provide for the sake of web marshalling tools that require all 
 * Java types to be public, concrete classes.  Unfortunately, the standard
 * SimpleEntry is a nested class, so not available to such tools.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class SimpleEntry<K, V> 
	extends java.util.AbstractMap.SimpleEntry<K, V> {
	
	/**
	 * Generates a different serialVersionUID for this extension
	 */
	private static final long serialVersionUID = -5377216819513763466L;

	public SimpleEntry(K key, V value) {
		super(key, value);
	}
	
	public SimpleEntry(Entry<? extends K, ? extends V> entry) {
		super(entry);
	}
	
}
