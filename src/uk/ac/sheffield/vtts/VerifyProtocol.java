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
 * Program that reads an XML service specification and checks the protocol of
 * the service for input and memory completeness and deterministic behaviour.  
 * Use this program to analyse the protocol directly, after the accompanying
 * state machine has already been validated.  Warnings are given for 
 * missing operation scenarios (that should exist, according to the state 
 * machine), for incompletely-initialised memory and for operations that are
 * found to be either blocking, or non-deterministic, under certain input and
 * memory conditions.  An analysis is given of the input and memory space of
 * each operation, showing which guarded scenario accepts which input.
 * Warnings identify inputs that trigger no response, or multiple responses.
 * 
 * Requires ASTReader, ASTWriter, ASTError, NodeError from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class VerifyProtocol {

	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, checks the protocol of the specification for input
	 * completeness.  For every operation, creates a set of input partitions,
	 * which includes every guard and its complement.  Then, determines
	 * whether each partition triggers exactly one response.  If no response
	 * is triggered, or more than one response is triggered, this indicates
	 * a possible fault in the specification.  Uses a symbolic subsumption
	 * checking algorithm, which is conservative.
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
		System.out.println("Starting program: VerifyProtocol.\n");

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

			String outputName = service.getName() + "Verification.xml";
			File outputFile = new File(directory, outputName);

			service.verifyProtocol();

			ASTWriter writer = null;
			try{
				writer = new ASTWriter(outputFile);
				writer.usePackage("uk.ac.sheffield.vtts.model");
				writer.writeDocument(service.getProtocol());
			}
			finally {
				if (writer != null) writer.close();
			}

			System.out.println("Marshalled the verification to output file: " + outputFile);
		}
		else
			System.out.println("Usage: java VerifyProtocol <specFile.xml>");

		System.out.println("\nProgram completed with success.");
	}

}
