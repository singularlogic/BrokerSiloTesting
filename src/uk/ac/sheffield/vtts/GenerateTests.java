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
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * Program that reads an XML service specification and generates a high-level
 * test suite from the specification.  Use this program to generate optimal
 * tests, stored in a technology-neutral XML format, after completing the
 * earlier validation and verification steps.  The program simulates the 
 * complete Stream X-Machine specification and explores all paths through
 * the state machine and protocol, up to a given depth.  Warnings are given
 * if states are unreachable, or if guarded transitions in the specification
 * are not covered by the generated test suite.  An analysis is given of the
 * test optimisation performed, which removes infeasible sequences (blocked
 * by guards), redundant sequences (covered by earlier tests) and optional
 * test-compression, achieved by merging shorter sequences with longer
 * sequences that have the shorter as a prefix.
 * 
 * Requires ASTReader, ASTWriter, ASTError, NodeError from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class GenerateTests {

	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, generates a high-level XML test suite, using the 
	 * test depth parameter to control the maximum length of test sequences
	 * to explore from every state, and the multi-test parameter to determine
	 * whether to merge simple tests into multi-objective tests.  Simulates
	 * the specification exhaustively to the given test depth, then prunes
	 * the resulting test sequences to create a feasible and non-redundant
	 * set of sequences, possibly compressed after multi-objective merging.
	 * The service specification may define the test depth and multi-test
	 * parameters, or these may optionally be supplied here.
	 * @param args the XML service specification file, containing a root
	 * Service node; and optionally an integer test depth (zero by default)
	 * and a boolean flag to indicate multi-objective testing (false by 
	 * default).  Values for the optional parameters may also be set in the
	 * service specification.
	 * @throws IOException if a file system related I/O error occurs.
	 * @throws IllegalArgumentException if an invalid argument is supplied.
	 * @throws ASTError if an XML syntax error is found in the input.
	 * @throws NodeError if marshalling or unmarshalling the model fails.
	 * @throws SemanticError if a semantic error is detected in the model.
	 */
	public static void main(String[] args) throws IOException, IllegalArgumentException,
			ASTError, NodeError, SemanticError {
		System.out.println("Starting program: GenerateTests.\n");

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
			
			System.out.println("Unmarshalled the model from input file: " + inputFile);

			String outputName = service.getName() + "Tests.xml";
			File outputFile = new File(directory, outputName);

			int testDepth = service.getTestDepth();		// if specified
			boolean multiTest = service.isMultiTest();	// if specified
			
			if (args.length > 1) {
				try {
					testDepth = Integer.parseInt(args[1]);
				}
				catch (NumberFormatException ex) {
					throw new IllegalArgumentException(
							"Second argument must be an integer test depth.");
				}
			}
			
			if (args.length > 2) {
				// Check explicitly, since Boolean.parseBoolean() is too lenient.
				if (! (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")))
					throw new IllegalArgumentException(
							"Third agrument must be a boolean multi-test flag.");
				multiTest = Boolean.parseBoolean(args[2]);
			}

			TestSuite testSuite = service.generateTests(testDepth, multiTest);

			ASTWriter writer = null;
			try{
				writer = new ASTWriter(outputFile);
				writer.usePackage("uk.ac.sheffield.vtts.model");
				writer.writeDocument(testSuite);
			}
			finally {
				if (writer != null) writer.close();
			}

			System.out.println("Marshalled the test suite to output file: " + outputFile);
		}
		else
			System.out.println("Usage: java GenerateTests <specFile.xml> [<testDepth:int> <multiTest:bool>]");

		System.out.println("\nProgram completed with success.");
	}

}
