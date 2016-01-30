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

package uk.ac.sheffield.vtts;

import java.io.File;
import java.io.IOException;

import org.jast.ast.ASTError;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;
import org.jast.ast.NodeError;

import uk.ac.sheffield.vtts.model.SemanticError;
import uk.ac.sheffield.vtts.model.Service;

/**
 * Program that reads an XML service specification and checks the finite 
 * state machine for state reachability and transition completeness.
 * Use this program to simulate the state machine directly.  Warnings are 
 * given for unreachable states and missing transitions (that should exist,
 * according to the protocol).  An analysis is given of which events are 
 * ignored in each state, in case the designer wishes to handle these events
 * explicitly with a transition, rather than leave the default transition,
 * which is interpreted as a null operation.
 * 
 * Requires ASTReader, ASTWriter, ASTError, NodeError from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class ValidateMachine {

	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, checks the finite state machine for transition
	 * completeness.  For every state, attempts to find a response to every
	 * possible event.  Reports those events for which no transition is 
	 * triggered in a given state.
	 * @param args the XML service specification file, containing a root
	 * Service node.
	 * @throws IOException if a file system related I/O error occurs.
	 * @throws IllegalArgumentException if an invalid argument is supplied.
	 * @throws ASTError if an XML syntax error is found in the input.
	 * @throws NodeError if marshalling or unmarshalling the model fails.
	 * @throws SemanticError if a semantic error is detected in the model.
	 */
	public static void main(String[] args) throws IOException, IllegalArgumentException,
			ASTError, NodeError, SemanticError {
		System.out.println("Starting program: ValidateMachine.\n");

		if (args.length > 0) {

			if (! args[0].endsWith(".xml")) {
				throw new IllegalArgumentException(
						"First argument must be an XML specification.");
			}
			
			File inputFile = new File(args[0]);
			File directory = inputFile.getParentFile();

			ASTReader reader = null;
			Service service = null;
			try {
				reader = new ASTReader(inputFile);
				reader.usePackage("uk.ac.sheffield.vtts.model");
				service = (Service) reader.readDocument();
			}
			catch (ClassCastException ex) {
				throw new NodeError("XML file must contain root element: Service");
			}
			finally {
				if (reader != null) reader.close();
			}

			System.out.println("Unmarshalled the specification from input file: " + inputFile);
			
			String outputName = service.getName() + "Validation.xml";
			File outputFile = new File(directory, outputName);

			service.validateMachine();

			ASTWriter writer = null;
			try{
				writer = new ASTWriter(outputFile);
				writer.usePackage("uk.ac.sheffield.vtts.model");
				writer.writeDocument(service.getMachine());
			}
			finally {
				if (writer != null) writer.close();
			}

			System.out.println("Marshalled the validation to output file: " + outputFile);
		}
		else
			System.out.println("Usage: java ValidateMachine <specFile.xml>");

		System.out.println("\nProgram completed with success.");

	}

}
