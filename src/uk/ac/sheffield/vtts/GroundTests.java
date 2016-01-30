/**
 * Broker@Cloud Verification and Testing Tool Suite. Copyright (C) 2015 Anthony
 * J H Simons and Raluca Lefticaru, University of Sheffield, UK. All rights
 * reserved. DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is "foreground software", developed as an output of the European
 * Union collaborative research project, "Broker@Cloud: enabling continuous
 * quality assurance and optimization in future enterprise cloud service
 * brokers", FP7-ICT-2011-8 no. 318392, and is licensed under the Apache
 * License, Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * The freedoms granted by the License to incorporate, redistribute, modify or
 * extend the software apply only to "foreground software" contributed by the
 * Broker@Cloud project; and not to any proprietary software, or "background
 * software" incorporated from other sources, which may be offered under
 * different terms of usage.
 *
 * Please contact the Department of Computer Science, University of Sheffield,
 * Regent Court, 211 Portobello, Sheffield S1 4DP, UK or visit
 * www.sheffield.ac.uk/dcs if you need additional information or have any
 * questions.
 */
package uk.ac.sheffield.vtts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jast.ast.ASTError;
import org.jast.ast.ASTReader;
import org.jast.ast.NodeError;

import uk.ac.sheffield.vtts.ground.Grounding;
import uk.ac.sheffield.vtts.ground.JavaGrounding;
import uk.ac.sheffield.vtts.ground.JaxRsGrounding;
import uk.ac.sheffield.vtts.ground.JaxWsGrounding;
import uk.ac.sheffield.vtts.model.SemanticError;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * Program that reads a high-level test suite and generates an executable JUnit
 * test driver. Use this program to ground high-level tests supplied in a
 * technology-neutral format, converting these into a specific concrete
 * executable format that is appropriate for a given service implementation
 * technology. We provide three translations, as a demonstration of how to build
 * your own. One option generates a JUnit driver for a POJO service; another
 * generates a JUnit driver for a JAX-WS SOAP service; and the third option
 * generates a JUnit driver for a JAX-RS REST service. All use JUnit as the
 * mechanism to run the concrete tests. There is an option to include or exclude
 * full state and transition checking, if the tested service exposes this
 * information in test-mode.
 *
 * Requires ASTReader, ASTError, NodeError from the JAST package.
 *
 * @author Raluca Lefticaru
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class GroundTests {

    /**
     * The root source directory in your Java IDE (Eclipse or NetBeans, etc).
     * This is by default the directory "src" which sits under the top-level
     * project directory in your Java IDE. Edit this constant, if you expect
     * your Java packages to live somewhere else; otherwise all Java package
     * names are presumed to correspond to a directory structure under this root
     * directory.
     */
    public static String SOURCE_CODE_ROOT = "src";

    /**
     * The client package path in your Java IDE (Eclipse or NetBeans, etc). This
     * is by default the package "uk.ac.sheffield.vtts.client", which sits under
     * the root source directory in your Java IDE. Sub-packages of this are used
     * to store Java clients for web services.
     */
    public static String CLIENT_PACKAGE_PATH = "uk.ac.sheffield.vtts.client";

    /**
     * The test package path in your Java IDE (Eclipse or NetBeans, etc). This
     * is by default the package "uk.ac.sheffield.vtts.test", which sits under
     * the root source directory in your Java IDE. Sub-packages of this are used
     * to store generated JUnit test-drivers for web services.
     */
    public static String TEST_PACKAGE_PATH = "uk.ac.sheffield.vtts.test";

    /**
     * The default dummy URI for a RESTful web service. This is provided for
     * demonstration purposes only, so that REST test-drivers may refer to a URI
     * for the REST service that they test. Edit this constant, if you wish to
     * test REST services on a particular REST server; or supply a URI as a
     * command-line argument.
     */
    public static String DEFAULT_REST_URI = "http://my.rest.server";

    /**
     * Reads a high-level XML test suite from the input file and, if no errors
     * are found, generates an output file containing the source code for a
     * JUnit test driver, using the grounding parameter to select a particular
     * grounding strategy, and the meta-check parameter to indicate whether to
     * generate extra service calls to verify full state and transition
     * behaviour. By default, generated code is placed in one of the standard
     * packages: uk.ac.sheffield.vtts.test.pojo, uk.ac.sheffield.vtts.test.ws,
     * or uk.ac.sheffield.vtts.test.rs, according to the grounding. If optional
     * package-name arguments are supplied, these are assumed to be (1st) the
     * target package for the generated JUnit driver, and (2nd-nth) source
     * packages containing the Java service client and other definitions. All of
     * these packages are then presumed to exist in an IDE, under a root source
     * code directory, SOURCE_ROOT (by default "src"). If a REST service is to
     * be tested, the optional parameters must also include a REST URI name,
     * starting with "http". The high-level test suite may define a preferred
     * grounding and value for meta-check; otherwise these values must be
     * supplied.
     *
     * @param args the XML high-level test suite file, containing a root
     * TestSuite node; and optionally, a concrete grounding format chosen from:
     * "Java" | "JAX-WS" | "JAX-RS" ("Java", by default), a boolean meta-check
     * flag indicating whether to verify all states and transitions (true, by
     * default); and after these, an optional service URI (needed for REST
     * grounding only) and 0..n Java package names in which the generated test
     * driver, and sources for the tested service client, are to be found (the
     * URI is distinguished by its "http" prefix; and any package names are
     * processed in order, with the first package assumed to be the target
     * package and further packages assumed to be source packages).
     * @throws IOException if a file system related I/O error occurs.
     * @throws IllegalArgumentException if an invalid argument is supplied.
     * @throws ASTError if an XML syntax error is found in the input.
     * @throws NodeError if marshalling or unmarshalling the model fails.
     * @throws SemanticError if a semantic error is detected in the model.
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException,
            ASTError, NodeError, SemanticError {
        System.out.println("Starting program: GroundTests.\n");

        if (args.length > 0) {

            if (!args[0].endsWith(".xml")) {
                throw new IllegalArgumentException(
                        "First argument must be an XML test suite.");
            }

            File inputFile = new File(args[0]);

            ASTReader reader = null;
            TestSuite testSuite = null;
            try {
                reader = new ASTReader(inputFile);
                reader.usePackage("uk.ac.sheffield.vtts.model");  // use model classes from here
                testSuite = (TestSuite) reader.readDocument();
            } catch (ClassCastException ex) {
                throw new NodeError("XML file must contain root element: TestSuite");
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            System.out.println("Unmarshalled the test suite from input file: " + inputFile);

            // Default values for all the generation parameters
            String grounding = testSuite.getGrounding();
            boolean metaCheck = testSuite.getMetaCheck();
            String targetPackage = null;
            String endpointUri = null;
            List<String> sourcePackages = new ArrayList<String>();

            if (args.length > 1) {
                grounding = args[1];  // validated below
            }

            if (args.length > 2) {
                // Check explicitly, since Boolean.parseBoolean() is too lenient.
                if (!(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
                    throw new IllegalArgumentException(
                            "Third agrument must be a boolean meta-check flag.");
                }
                metaCheck = Boolean.parseBoolean(args[2]);
            }

            if (args.length > 3) {
                for (int i = 3; i < args.length; ++i) {
                    String option = args[i];
                    if (option.startsWith("http")) {
                        endpointUri = option;
                    } else if (targetPackage == null) {
                        targetPackage = option;
                    } else {
                        sourcePackages.add(option);
                    }
                }
            }

            // If none were supplied, use default source and target packages
            if (targetPackage == null || sourcePackages.isEmpty()) {
                String pkgExt;  // package extension for locating generated code
                if (grounding.equals("JAX-WS")) {
                    pkgExt = ".ws";
                } else if (grounding.equals("JAX-RS")) {
                    pkgExt = ".rs";
                } else {
                    pkgExt = ".pojo";
                }
                if (targetPackage == null) {
                    targetPackage = TEST_PACKAGE_PATH + pkgExt;  // Test driver package
                }
                if (sourcePackages.isEmpty()) {
                    sourcePackages.add(CLIENT_PACKAGE_PATH + pkgExt); // Java client package
                }
            }

            if (endpointUri == null) {
                endpointUri = DEFAULT_REST_URI;
            }

            File directory = new File(new File(SOURCE_CODE_ROOT),
                    targetPackage.replace('.', File.separatorChar));

            File f1 = new File(".");
            System.out.println("currentdir: "+f1.getAbsolutePath());
            
            String outputName = testSuite.getTestDriver() + ".java";
            System.out.println("directory: "+directory+" outputname:"+outputName);
            
            File outputFile = new File(directory, outputName);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new FileWriter(outputFile), true);
                Grounding visitor = null;
                if (grounding.equals("Java")) {
                    visitor = new JavaGrounding(writer);
                } else if (grounding.equals("JAX-WS")) {
                    visitor = new JaxWsGrounding(writer);
                } else if (grounding.equals("JAX-RS")) {
                    visitor = new JaxRsGrounding(writer);
                } else {
                    throw new IllegalArgumentException(
                            "Second argument must be a supported grounding.");
                }

                visitor.setMetaCheck(metaCheck);
                visitor.useEndpoint(endpointUri);
                visitor.useTargetPackage(targetPackage);
                for (String sourcePackage : sourcePackages) {
                    visitor.useSourcePackage(sourcePackage);
                }

                testSuite.receive(visitor);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            System.out.println("Generated Java tests written to output file: " + outputFile);
        } else {
            System.out.print("Usage: java GroundTests <testFile.xml> [<grounding:enum> <metaCheck:bool>");
            System.out.println(" <endpoint:uri>? <package>*]");
        }

        System.out.println("\nProgram completed with success.");
    }

}
