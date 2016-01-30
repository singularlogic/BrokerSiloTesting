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

package uk.ac.sheffield.vtts.client.pojo;

import java.util.ArrayList;
import java.util.List;

import uk.ac.sheffield.vtts.ground.SimpleEntry;

/**
 * ContactList is a correct POJO implementation of the 
 * <code>ContactList.xml</code>
 * specification.  It models a table of names, where names may be entered,
 * selected and removed.  This example was developed to mimic the behaviour
 * of many GUIs, where the selected row can be either transferred or dropped,
 * when a row is deleted.  This leads to three different kinds of behaviour
 * upon deletion, which are triggered by the filled-state and selection-state
 * of the table, rather than by any condition on the inputs.
 * <p>
 * This uses the State Pattern (a Design Pattern from Gamma, et al.) for its 
 * implementation.  All requests are delegated to an abstract State, which 
 * has concrete subclasses EmptyTable, NonEmptyTable and RowSelected, which 
 * respond to certain requests in different ways.  Otherwise, the table is
 * implemented as a List of SimpleEntry pairs, where each pair contains two 
 * Strings for the forename and surname.  Pairs in the specification language
 * are implemented as SimpleEntry in the Java code.  We provide a top-level
 * public type with this name, shadowing the inner class AbstractMap.SimpleEntry, 
 * which causes problems for SOAP and REST web service frameworks that cannot
 * pass nested classes as parameters.
 * <p>
 * Indexing in the specification language runs from 1..n; this is observed in
 * the values assigned to indicate the selected row, but indices are converted
 * into the range 0..n-1 when accessing the underlying Java Lists.
 * <p>
 * Suggestions are given for how to modify the source code to seed
 * faults deliberately, which will be detected during testing.
 *
 * @author Anthony J H Simons
 * @version 1.0
 */
public class ContactList {

	/**
	 * Logs the last request received by this ContactList.
	 */
	private String request;
	
	/**
	 * Logs the last response enacted by this ContactList.
	 */
	private String response;
	
	/**
	 * The last state entered by this ContactList.
	 */
	private State state;
	
	/**
	 * The list of contacts in this ContactList.  Uses the public Java type
	 * SimpleEntry<T, T> to model a pair of Strings, standing for a forename
	 * and a surname.  This SimpleEntry is a top-level type that shadows and
	 * trivially extends the nested inner type: java.util.Map.SimpleEntry,
	 * which causes problems for JAX-WS and JAX-RS implementations.
	 * @see uk.ac.sheffield.vtts.ground.SimpleEntry.
	 */
	private List<SimpleEntry<String, String>> contacts;
	
	/**
	 * The selected row (1..n), zero if no row selected.
	 */
	private int selectedRow;
	
	/**
	 * Creates a ContactList.  Enacts the scenario create/ok and enters the
	 * EmptyTable state.  Initialises the lists of forenames and surnames to
	 * empty lists, and sets the selected row to zero (logical indexing runs
	 * from 0..n).
	 */
	public ContactList() {
		// implementation
		contacts = new ArrayList<SimpleEntry<String, String>>();
		selectedRow = 0;  // indexing is 1..n
		state = new EmptyTable();  // initial state
		// logging info
		request = "create";
		response = "ok";
	}

	/**
	 * Returns the last scenario that was enacted.  This State Pattern
	 * implementation logs separately the request received and the response
	 * triggered (which is state-dependent).  The name of the last scenario
	 * that was enacted is obtained by concatenating the request, "/" and 
	 * the response.
	 * @return the last scenario that was enacted.
	 */
	public String getScenario() {
		return request + "/" + response;
	}

	/**
	 * Returns the last state that was entered.  This State Pattern 
	 * implementation models the states explicitly as nested inner classes, 
	 * which also have access to the ContactList' fields.  The name of the
	 * current state is obtained by reflection on the class name.
	 * @return the name of the current State instance.
	 */
	public String getState() {
		return state.getClass().getSimpleName();
	}

	/**
	 * Attempts to add a new entry to this ContactList.  If the forename and
	 * surname are valid (non-empty) Strings, enacts addEntry/ok and inserts
	 * the forename and surname at the end of their respective lists; and, if
	 * the current state is EmptyTable, changes state to NonEmptyTable, 
	 * otherwise remains in the same state (NonEmptyTable, or RowSelected).
	 * If one of the names is an empty String, enacts addEntry/error and 
	 * remains in the same state.
	 * @param forename a forename.
	 * @param surname a surname.
	 */
	public void addEntry(String forename, String surname) {
		request = "addEntry";
		state.addEntry(forename, surname);
	}

	/**
	 * Attempts to select a row in this ContactList.  If in the NonEmptyTable
	 * or RowSelected state, such that there are 1..n rows to be selected, and 
	 * a valid row index is chosen, enacts either selectRow/high, if row n was
	 * selected, or selectRow/low if a lower row was selected, and then enters
	 * the RowSelected state; otherwise enacts selectRow/error and remains in 
	 * the same state.
	 * @param index the row selection index.
	 */
	public void selectRow(int index) {
		request = "selectRow";
		state.selectRow(index);
	}

	/**
	 * Attempts to unselect a row in this ContactList.  If in the RowSelected
	 * state, where a current row is selected, enacts unselectRow/ok, sets the
	 * selected row to zero and changes state to NonEmptyTable; otherwise 
	 * enacts unselectRow/error and remains in the same state (EmptyTable, or
	 * NonEmptyTable).
	 */
	public void unselectRow() {
		request = "unselectRow";
		state.unselectRow();
	}

	/**
	 * Attempts to remove an entry from this ContactList.  If in the state
	 * RowSelected, enacts one of three scenarios, depending on the table's
	 * fill-level and the row selected.  If there is only one entry in the
	 * table, enacts removeEntry/onlyEntry, deletes the names from their
	 * respective arrays, sets the row selected to zero and enters the 
	 * EmptyTable state.  If there are multiple entries and the highest
	 * entry was selected, enacts removeEntry/finalEntry, deletes the names
	 * from their respective arrays, sets the row selected to zero and enters
	 * the NonEmptyTable state.  Otherwise, enacts removeEntry/medialEntry
	 * and deletes the names, but allows the selection to be transferred onto
	 * the next row, and remains in the RowSelected state.  If in any other
	 * state, enacts removeEntry/error and remains in that state.
	 */
	public void removeEntry() {
		request = "removeEntry";
		state.removeEntry();
	}
	
	/**
	 * State describes the default behaviour for all states.  This is
	 * selectively overridden in concrete subclasses.  It is always
	 * possible to add a new entry in any state; but there must be some
	 * existing entries for selection and deletion to be possible.
	 *
	 * @author Anthony J H Simons
	 * @version 1.0
	 */
	private abstract class State {
		public void addEntry(String forename, String surname) {
			if (forename.isEmpty() || surname.isEmpty()) {
				response = "error";
			}
			else {
				contacts.add(new 
						SimpleEntry<String, String>(forename, surname));
				response = "ok";
			}
		}
		public void selectRow(int row) {
			response = "error";
		}
		public void unselectRow() {
			response = "error";
		}
		public void removeEntry() {
			response = "error";
		}
	}
	
	/**
	 * EmptyTable has the default behaviour, except for changing state to a
	 * NonEmptyTable after adding a valid entry.  Select row, unselect row 
	 * and remove entry are all disabled in this state.
	 *
	 * @author Anthony J H Simons
	 * @version 1.0
	 */
	private class EmptyTable extends State {
		public void addEntry(String forename, String surname) {
			super.addEntry(forename, surname);
			if (response.equals("ok"))
				state = new NonEmptyTable();
		}
	}
	
	/**
	 * NonEmptyTable may have multiple entries, but no current selected row.
	 * Adding an entry leaves the state unchanged; but selecting a row 
	 * changes the state to RowSelected.  Select row has three responses:
	 * error, if the selection is out of range; high, if the highest row is
	 * selected; and low, if a lower row is selected.  Unselect row and 
	 * remove entry are disabled.
	 *
	 * @author Anthony J H Simons
	 * @version 1.0
	 */
	private class NonEmptyTable extends State {
		public void selectRow(int row) {
			if (row > 0 && row <= contacts.size()) {
				response = (row == contacts.size() ? "high" : "low");
				selectedRow = row;
				state = new RowSelected();
			}
			else {
				response = "error";
			}
		}
	}
	
	/**
	 * RowSelected may have multiple entries and a row is always selected.
	 * Adding an entry and selecting a row remain in this state, but select
	 * row may modify the row selected.  Unselect row will change state to
	 * NonEmptyTable.  Remove entry may remain in this state (if the removed
	 * row is medial and the selection is transferred); or change state to
	 * NonEmptyTable (if the highest row was selected, and the selection
	 * cannot be transferred); or change state to EmptyTable (if there was
	 * only one entry in the table).  This mimics the behaviour of many 
	 * list-selection UIs.
	 *
	 * @author Anthony J H Simons
	 * @version 1.0
	 */
	private class RowSelected extends State {
		public void selectRow(int row) {
			if (row > 0 && row <= contacts.size()) {
				response = (row == contacts.size() ? "high" : "low");
				selectedRow = row;
			}
			else {
				response = "error";
			}
		}
		public void unselectRow() {
			response = "ok";
			selectedRow = 0;
			state = new NonEmptyTable();
		}
		public void removeEntry() {
			if (contacts.size() == 1) {
				response = "onlyEntry";
				contacts.remove(0);
				selectedRow = 0;
				state = new EmptyTable();
			}
			else if (selectedRow < contacts.size()) {
				response = "medialEntry";
				contacts.remove(selectedRow -1);
				// selection is transferred; stay in this state
			}
			else {
				response = "finalEntry";
				contacts.remove(selectedRow -1);
				selectedRow = 0;
				state = new NonEmptyTable();
			}
		}

	}

}
