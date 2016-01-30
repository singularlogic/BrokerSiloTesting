
package eu.singularlogic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;
import org.jast.ast.NodeError;
import static uk.ac.sheffield.vtts.GroundTests.CLIENT_PACKAGE_PATH;
import static uk.ac.sheffield.vtts.GroundTests.DEFAULT_REST_URI;
import static uk.ac.sheffield.vtts.GroundTests.SOURCE_CODE_ROOT;
import static uk.ac.sheffield.vtts.GroundTests.TEST_PACKAGE_PATH;
import uk.ac.sheffield.vtts.ground.Grounding;
import uk.ac.sheffield.vtts.ground.JavaGrounding;
import uk.ac.sheffield.vtts.ground.JaxRsGrounding;
import uk.ac.sheffield.vtts.ground.JaxWsGrounding;
import uk.ac.sheffield.vtts.model.Service;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 *
 * @author vmadmin
 */
public class Orchestrate {

    public static void main(String[] args) {
        try {
//            //step1 validate
//            Orchestrate.step1Validate("VatClearance.xml", "uk.ac.sheffield.vtts.model");      //uk.ac.sheffield.vtts.model   eu.singularlogic.model
//            //step2 verify
//            Orchestrate.step2Verify("VatClearance.xml", "uk.ac.sheffield.vtts.model");
//            //step3 generate tests
//            Orchestrate.step3GenerateTests("VatClearance.xml", "uk.ac.sheffield.vtts.model", 1, false);
//            step4 ground tests
//            Orchestrate.step4GroundTests("VatClearanceTests.xml", "Java",true,"http://mpampa",null,null);
        } catch (Exception ex) {
            Logger.getLogger(Orchestrate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads an XML service specification from the input file, and, if no faults
     * are found, checks the finite state machine for transition completeness.
     * For every state, attempts to find a response to every possible event.
     * Reports those events for which no transition is triggered in a given
     * state.
     *
     * @param args the XML service specification file, containing a root Service
     * node.
     * @throws IOException if a file system related I/O error occurs.
     * @throws IllegalArgumentException if an invalid argument is supplied.
     * @throws ASTError if an XML syntax error is found in the input.
     * @throws NodeError if marshalling or unmarshalling the model fails.
     * @throws SemanticError if a semantic error is detected in the model.
     */
    public static void step1Validate(String specfilename, String packagename) throws IOException {

        ASTReader reader = null;
        Service service = null;
        ASTWriter writer = null;

        try {
//            File fl = new File("test");
//            System.out.println(fl.getAbsolutePath());
            File inputFile = new File(specfilename);
            File directory = inputFile.getParentFile();
            reader = new ASTReader(inputFile);
            reader.usePackage("uk.ac.sheffield.vtts.model");
            service = (Service) reader.readDocument();
            System.out.println("Unmarshalled the specification from input file: " + inputFile);

            String outputName = service.getName() + "Validation.xml";
            File outputFile = new File(directory, outputName);
            service.validateMachine();

            writer = new ASTWriter(outputFile);
            writer.usePackage(packagename);
            writer.writeDocument(service.getMachine());
            writer.close();
            System.out.println("Marshalled the validation to output file: " + outputFile);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }

    }//step1

    /**
     * Program that reads an XML service specification and checks the protocol
     * of the service for input and memory completeness and deterministic
     * behaviour. Use this program to analyse the protocol directly, after the
     * accompanying state machine has already been validated. Warnings are given
     * for missing operation scenarios (that should exist, according to the
     * state machine), for incompletely-initialised memory and for operations
     * that are found to be either blocking, or non-deterministic, under certain
     * input and memory conditions. An analysis is given of the input and memory
     * space of each operation, showing which guarded scenario accepts which
     * input. Warnings identify inputs that trigger no response, or multiple
     * responses.
     *
     * Requires ASTReader, ASTWriter, ASTError, NodeError from the JAST package.
     *
     * @author Anthony J H Simons
     * @version Broker@Cloud 1.0
     */
    public static void step2Verify(String specfilename, String packagename) {

        System.out.println("Starting program: VerifyProtocol.\n");

        ASTReader reader = null;
        Service service = null;
        try {
            File inputFile = new File(specfilename);
            File directory = inputFile.getParentFile();
            reader = new ASTReader(inputFile);
            reader.usePackage(packagename);
            service = (Service) reader.readDocument();
            System.out.println("Unmarshalled the specification from input file: " + inputFile);
            String outputName = service.getName() + "Verification.xml";
            File outputFile = new File(directory, outputName);
            service.verifyProtocol();
            ASTWriter writer = null;
            writer = new ASTWriter(outputFile);
            writer.usePackage("uk.ac.sheffield.vtts.model");
            writer.writeDocument(service.getProtocol());
            writer.close();
            System.out.println("Marshalled the verification to output file: " + outputFile);
            System.out.println("\nProgram completed with success.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }//step2    

    /**
     * Reads an XML service specification from the input file, and, if no faults
     * are found, generates a high-level XML test suite, using the test depth
     * parameter to control the maximum length of test sequences to explore from
     * every state, and the multi-test parameter to determine whether to merge
     * simple tests into multi-objective tests. Simulates the specification
     * exhaustively to the given test depth, then prunes the resulting test
     * sequences to create a feasible and non-redundant set of sequences,
     * possibly compressed after multi-objective merging. The service
     * specification may define the test depth and multi-test parameters, or
     * these may optionally be supplied here.
     *
     * @param args the XML service specification file, containing a root Service
     * node; and optionally an integer test depth (zero by default) and a
     * boolean flag to indicate multi-objective testing (false by default).
     * Values for the optional parameters may also be set in the service
     * specification.
     * @throws IOException if a file system related I/O error occurs.
     * @throws IllegalArgumentException if an invalid argument is supplied.
     * @throws ASTError if an XML syntax error is found in the input.
     * @throws NodeError if marshalling or unmarshalling the model fails.
     * @throws SemanticError if a semantic error is detected in the model.
     */
    public static void step3GenerateTests(String specfilename, String packagename, int atestDepth, boolean amultiTest) {

        ASTReader reader = null;
        Service service = null;
        try {
            File inputFile = new File(specfilename);
            File directory = inputFile.getParentFile();

            reader = new ASTReader(inputFile);
            reader.usePackage(packagename);
            service = (Service) reader.readDocument();

            System.out.println("Unmarshalled the model from input file: " + inputFile);

            String outputName = service.getName() + "Tests.xml";
            File outputFile = new File(directory, outputName);

            int testDepth = atestDepth;		// if specified
            boolean multiTest = amultiTest;          // if specified            

            TestSuite testSuite = service.generateTests(testDepth, multiTest);
            ASTWriter writer = null;
            writer = new ASTWriter(outputFile);
            writer.usePackage(packagename);
            writer.writeDocument(testSuite);
            writer.close();
            System.out.println("Marshalled the test suite to output file: " + outputFile);
            System.out.println("\nProgram completed with success.");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }

    }//step3

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
    public static void step4GroundTests(String testfilename, String agrounding, boolean ametacheck, String aendpoint,String atargetpackage, String asourcepackage) { //agrounding= Java, JAX-WS, JAX-RS

        ASTReader reader = null;
        TestSuite testSuite = null;
        try {
            File inputFile = new File(testfilename);
            reader = new ASTReader(inputFile);
            reader.usePackage("uk.ac.sheffield.vtts.model");  // use model classes from here
            testSuite = (TestSuite) reader.readDocument();

            System.out.println("Unmarshalled the test suite from input file: " + inputFile);

            // Default values for all the generation parameters
            String grounding = testSuite.getGrounding();
            boolean metaCheck = testSuite.getMetaCheck();
            String targetPackage = null;
            String endpointUri = null;
            List<String> sourcePackages = new ArrayList<String>();

            grounding = agrounding;  // validated below
            metaCheck = ametacheck;
            endpointUri = aendpoint;
            //targetPackage = atargetpackage;
            //sourcePackages.add(asourcepackage);
            
            

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

            String outputName = testSuite.getTestDriver() + ".java";
            File outputFile = new File(directory, outputName);
            System.out.println("directory:"+directory+" outputName: "+outputName);
            
            PrintWriter writer = null;
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

            System.out.println("Generated Java tests written to output file: " + outputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }//EoM

}//EoC
