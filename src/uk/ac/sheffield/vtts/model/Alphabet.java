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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Alphabet represents the complete set of events recognised by a machine.
 * The Alphabet of a Machine is created by exploring all the Transitions
 * of a Machine and including an Event for each labelled Transition.  The
 * Alphabet is used to construct Languages of different lengths, the basis
 * for sequences presented to a Machine.  The Alphabet of a Protocol is
 * calculated by exploring all the Operations and Scenarios in the Protocol
 * and including an Event for each labelled Scenario.  Alphabets may be
 * compared, to see whether the Machine and the Protocol handle the same
 * set of Events.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Alphabet extends Element {
	
	/**
	 * The set of events in this alphabet.
	 */
	private Set<Event> events;

	/**
	 * Creates an empty Alphabet.
	 */
	public Alphabet() {
		events = new LinkedHashSet<Event>();
	}
		
	/**
	 * Reports whether this Alphabet is empty.
	 * @return true if this Alphabet is empty.
	 */
	public boolean isEmpty() {
		return events.isEmpty();
	}
	
	/**
	 * Returns the size of this alphabet.
	 * @return the number of distinct events.
	 */
	public int size() {
		return events.size();
	}

	/**
	 * Adds an event to this alphabet.  If this alphabet does not already
	 * contain an event equal to the added event, it includes the new event
	 * in its set of events.
	 * @param event the event.
	 * @return this alphabet.
	 */
	public Alphabet addEvent(Event event) {
		events.add(event);
		return this;
	}

	/**
	 * Returns the set of events in this alphabet.
	 * @return the set of events.
	 */
	public Set<Event> getEvents() {
		return events;
	}
	
	/**
	 * Computes the difference between this and the other Alphabet.  This
	 * is used when comparing two Alphabets for consistency.  The result
	 * is a new Alphabet, containing the Events from this Alphabet that
	 * were not found in the other Alphabet.
	 * @param other the other Alphabet.
	 * @return the Alphabet representing the difference.
	 */
	public Alphabet subtract(Alphabet other) {
		Alphabet result = new Alphabet();
		for (Event event : events) {
			if (! other.events.contains(event))
				result.addEvent(event);
		}
		return result;
	}

}
