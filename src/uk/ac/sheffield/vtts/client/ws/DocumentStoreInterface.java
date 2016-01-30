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

package uk.ac.sheffield.vtts.client.ws;

import java.util.List;

/**
 * DocumentStoreInterface is a skeleton of the JAX-WS client interface for
 * the JAX-WS implementation of the <code>DocumentStore.xml</code> example.
 * This is a placeholder until you develop your own interface using JAX-WS
 * tools.
 *
 * @author Anthony J H Simons
 * @version 1.0
 */
public interface DocumentStoreInterface {

	/**
	 * Resets the service to its clean initial state.
	 */
	public void reset();

	/**
	 * Returns the last scenario that was enacted.
	 * @return the last scenario that was enacted.
	 */
	public String getScenario();

	/**
	 * Returns the last state that was entered.
	 * @return the last state that was entered.
	 */
	public String getState();

	/**
	 * Attempts to login with the given credentials.
	 * @param username the customer's name.
	 * @param password the customer's password.
	 * @return a list of the storage allocation and encryption standard.
	 */
	public List<Object> login(String username, String password);

	/**
	 * Logs out of the service, if logged in.
	 */
	public void logout();

	/**
	 * Returns the encryption standard for the customer.
	 * @return the encryption standard, or null if ignored.
	 */
	public Integer getEncryption();

	/**
	 * Returns the storage allocation limit for the customer.
	 * @return the storage limit, or null if ignored.
	 */
	public Integer getStorageLimit();

	/**
	 * Returns the storage used by the customer.
	 * @return the storage used, or null if ignored.
	 */
	public Integer getStorageUsed();

	/**
	 * Attempts to store a document under the docid.  Fails if the docid is
	 * out of range, or the document is too large.  If the docid is already
	 * known, adds the document as the latest version.  Otherwise starts a
	 * new document history with the document as the first version. 
	 * @param docid the document index.
	 * @param document the Document.
	 * @return a list of the remaining storage; and the latest version or
	 * null, if not stored.
	 */
	public List<Object> putDocument(Integer docid, Document document);

	/**
	 * Attempts to retrieve a document under the docid.  Fails if the docid
	 * is out of range, or no versions exist.  Otherwise returns the latest
	 * version of that document.
	 * @param docid the document index.
	 * @return the latest version of the Document.
	 */
	public Document getDocument(Integer docid);

	/**
	 * Attempts to retrieve a given version of a document with the given
	 * docid.  Fails if the docid is out of range, or the version is out of
	 * range.  Otherwise returns that version of the document.
	 * @param docid the document index.
	 * @param version the version index.
	 * @return the document, or null if ignored.
	 */
	public Document getVersion(Integer docid, Integer version);

	/**
	 * Attempts to delete a given version of a document with the given docid.
	 * Fails if the docid is out of range or the version is out of range.
	 * Otherwise, deletes the version of the document and returns the new
	 * amount of remaining storage.
	 * @param docid the document index.
	 * @param version the version index.
	 * @return the remaining storage.
	 */
	public Integer deleteVersion(Integer docid, Integer version);

}
