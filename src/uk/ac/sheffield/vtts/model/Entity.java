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
 * Entity is the model entity used to simulate all uninterpreted basic types.
 * A specification may wish to model uninterpreted basic types, such as 
 * Document, or Customer.  Such types are uninterpreted, and when simulated,
 * are modelled by an instance of Entity.  The only thing known about an
 * Entity is its name, which serves as a unique identifier.  The value
 * of a declared Constant of some uninterpreted type is used as the name
 * of the Entity, which is simulated in the specification.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Entity extends Named {

	/**
	 * Creates an unnamed Entity.  There can only be one such instance;
	 * but in practice, Entities are created with names.
	 */
	public Entity() {
	}

	/**
	 * Creates an Entity with the given name.  This name is used to identify
	 * the Entity, in the way it is stored in Maps and Sets, or compared for
	 * equality with other Entities.
	 * @param name the name of this Entity.
	 */
	public Entity(String name) {
		super(name);
	}

}
