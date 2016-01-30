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
 * Event represents a unique named event recognised by a finite state machine.
 * A finite state Machine recognises a set of uniquely named Events, known as
 * the Alphabet of the Machine.  Each Event is a symbolic request/response 
 * combination, denoting a distinct kind of expected behaviour in the finite 
 * state Machine.  An ordered presentation of Events is known as a Sequence.
 * An Event is deemed equal to another Event if they have the same name.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Event extends Named {
	
	/**
	 * Validates the name of an Event.  The name of an Event should be in the
	 * format: "request/response".
	 * @param name the name of the Event.
	 */
	protected void validate(String name) {
		if (name == null || name.length() < 3 || name.indexOf('/') == -1)
			semanticError("incorrectly named as '" + name +
					"'; should be like: 'request/response'.");
	}
	
	/**
	 * Creates default Event.
	 */
	public Event() {
	}
	
	/**
	 * Creates a named Event.  Validates the name, to ensure that it is of the
	 * format: "request/response".
	 * @param name the name of this Event.
	 */
	public Event(String name) {
		super(name);
		validate(name);
	}
	
	/**
	 * The request-name part of this Event's name.
	 * @return the request-name.
	 */
	public String requestName() {
		return name.split("/")[0];
	}
		
	/**
	 * The response-name part of this Event's name.
	 * @return the response-name.
	 */
	public String responseName() {
		return name.split("/")[1];
	}
	
}
