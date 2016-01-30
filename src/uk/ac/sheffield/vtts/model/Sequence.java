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
import java.util.List;

/**
 * Sequence represents a sequence of events to present to a finite state 
 * machine.  A Sequence consists of a list of Events, where each Event is a
 * symbolic request/response pair, denoting a particular expected kind of
 * behaviour in a finite state Machine.  A collection of Sequences forms a 
 * Language.  A Sequence is equal to another Sequence if it consists of
 * the same ordered list of Events.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class Sequence extends Element {

	/**
	 * The sequence of events.
	 */
	protected List<Event> events;
	
	/**
	 * The name of the State to be covered by this Sequence.
	 */
	protected String state;
	
	/**
	 * The length of the path to explore from the covered State.
	 */
	protected int path = 0;

	/**
	 * Creates an empty sequence.
	 */
	public Sequence() {
		events = new ArrayList<Event>();
	}
	
	/**
	 * Copies another sequence.  The new sequence is a shallow copy of the
	 * old sequence, sharing all of its events.  This constructor is used
	 * when extending a Sequence to create longer Sequences.
	 * @param other the other sequence.
	 */
	public Sequence(Sequence other) {
		events = new ArrayList<Event>(other.events);
		state = other.state;
		path = other.path;
	}
	
	/**
	 * Reports whether this sequence is equal to another object.  True,
	 * if the other object is a sequence consisting of the same events
	 * ordered in the same sequence.
	 * @param other the other object.
	 * @return true, if both objects are sequences with the same events.
	 */
	@Override
	public boolean equals(Object other) {
		try {
			Sequence sequence = (Sequence) other;
			return events.equals(sequence.events);
		}
		catch (ClassCastException ex) {
			return false;
		}
	}
	
	/**
	 * Returns a quasi-unique hash code for this sequence.  Returns the hash
	 * code associated with this sequence's list of events.
	 * @return the hash code for this sequence.
	 */
	@Override
	public int hashCode() {
		return events.hashCode();
	}
	
	/**
	 * Returns the size of this sequence.
	 * @return the length of this sequence.
	 */
	public int size() {
		return events.size();
	}
	
	/**
	 * Reports whether this is the empty Sequence.
	 * @return true, if this Sequence is empty.
	 */
	public boolean isEmpty() {
		return events.isEmpty();
	}

	/**
	 * Adds an event to the end of this sequence.  Modifies this sequence by
	 * adding an event to the end of this sequence's list of events.
	 * @param event a named event.
	 * @return this sequence.
	 */
	public Sequence addEvent(Event event) {
		events.add(event);
		return this;
	}
	
	/**
	 * Concatenates another sequence onto the end of this sequence.  Modifies
	 * this sequence by concatenating all the events from the other sequence,
	 * in order, onto the end of this sequence's list of events.  The result
	 * is a longer sequence which contains this sequence as a prefix and the
	 * other sequence as a suffix.  The other sequence is unchanged.
	 * @param other the other sequence.
	 * @return this sequence.
	 */
	public Sequence addSequence(Sequence other) {
		events.addAll(other.getEvents());
		return this;
	}

	/**
	 * Returns the ordered list of events.
	 * @return the list of Events.
	 */
	public List<Event> getEvents() {
		return events;
	}
	
	/**
	 * Returns the first event in this Sequence.  If this Sequence is empty,
	 * returns null, otherwise returns the first Event.
	 * @return the last Event, or null.
	 */
	public Event getFirstEvent() {
		if (isEmpty())
			return null;
		else
			return events.get(0);
	}

	/**
	 * Returns the last event in this Sequence.  If this Sequence is empty,
	 * returns null, otherwise returns the last Event.
	 * @return the last Event, or null.
	 */
	public Event getLastEvent() {
		if (isEmpty())
			return null;
		else
			return events.get(events.size() -1);
	}

	/**
	 * Returns the name of the state covered by this Sequence.
	 * @return the name of the state covered by this Sequence.
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the name of the state covered by this Sequence.
	 * @param state the name of the State to set.
	 * @return this Sequence.
	 */
	public Sequence setState(String state) {
		this.state = state;
		return this;
	}

	/**
	 * Returns the length of the path explored from the covered State.
	 * @return the length of the path explored from the covered State.
	 */
	public int getPath() {
		return path;
	}

	/**
	 * Sets the length of the path explored from the covered State.
	 * @param path the path length to set.
	 * @return this Sequence.
	 */
	public Sequence setPath(int path) {
		this.path = path;
		return this;
	}
		
}
