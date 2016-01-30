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

package uk.ac.sheffield.vtts.ground;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Set;

import uk.ac.sheffield.vtts.model.Failure;
import uk.ac.sheffield.vtts.model.Input;
import uk.ac.sheffield.vtts.model.Notice;
import uk.ac.sheffield.vtts.model.Operation;
import uk.ac.sheffield.vtts.model.Output;
import uk.ac.sheffield.vtts.model.TestSequence;
import uk.ac.sheffield.vtts.model.TestStep;
import uk.ac.sheffield.vtts.model.TestSuite;

/**
 * JaxRsGrounding assumes that the tested artifact is a REST web service that
 * was created using JAX-RS.  We assume the <em>Apache Jersey</em> reference
 * implementation of a JAX-RS REST service and <em>@PathParam</em> as the 
 * mechanism for marshalling simple input parameters, which are added to the 
 * REST URL.  We expect the service to return results in the JSON format.  We
 * use the <em>Google Gson</em> parser to unmarshal results.  
 * 
 * We assume the REST service is offered through a root URI in the format:
 * <em>http://your_domain:port/display-name/url-pattern/service_class/</em>, 
 * where <EM>display-name</em> and <em>url-pattern</em> are elements used in
 * the <em>web.xml</em> configuration file.  This root URI must be set using 
 * the method <em>useEndpoint()</em>.  It is also possible to specify where
 * to place the generated Test Driver with <em>useTargetPackage()</em> and
 * where to find the JAX-RS client implementation, and any other user-defined
 * types, with <em>useSourcePacakge</em> for each included package.
 * 
 * Calls against the REST service are built by extending the root URI with
 * the name of an operation and any further input parameters, separated by a
 * forward slash. Operations expect to be invoked using the HTTP POST method.
 * If full testing of visited states and exercised transitions is requested, 
 * by <em>setMetaCheck(true)</em>, the service must provide two additional 
 * methods <em>getState()</em> and <em>getScenario()</em>, which expect to be
 * invoked using the HTTP GET method.  The JAX-RS service implementation must
 * annotate the corresponding methods with @POST and @GET, respectively.
 * A JaxWsGrounding is supplied at creation with the PrintWriter to use for 
 * streaming the output.
 * 
 * @author Raluca Lefticaru
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class JaxRsGrounding extends AbstractGrounding {
	
	/**
	 * The root URI for the REST web service.  This URI must have the format:
	 * <em>http://your_domain:port/display-name/url-pattern/service_class/</em>
	 */
	private String endpoint;
	
	/**
	 * Creates a JaxRsGrounding writing to a stream.  By default, the
	 * target package is the default (unnamed) Java package.
	 * @param stream the output stream.
	 */
	public JaxRsGrounding(PrintWriter stream) {
		super(stream);
	}
		
	/**
	 * Translates an abstract TestSuite into a grounded suite of tests.
	 * @param testSuite the abstract TestSuite.
	 */
	@Override
	public void groundTestSuite(TestSuite testSuite)  {
		// Calculate package dependencies
		dependency.analyse(testSuite);
		// Create the file preamble, including current and included packages
		writePackageInfo(testSuite);
		writeClassComment(testSuite);
		
		String driver = testSuite.getTestDriver();
		// If nothing is used from uk.ac.sheffield.vtts.client.rs
		// writer.println("@SuppressWarnings(\"unused\")");

		writer.println("public class " + driver + " {");
		
		writeDriverFields(testSuite);
		writeDriverConstructor(testSuite);
		writeSystemSetUp(testSuite);
		
		writeCallMethod();		// extra method, just for RESTful grounding
		writeStatusMethods();	// extra method, just for RESTful grounding
		
		// The main loop over test sequences
		testIndex = 1;  // first testIndex == 1
		for (TestSequence sequence : testSuite.getTestSequences()) {
			sequence.receive(this);
			++testIndex;  
		}
		
		writer.println("}");
		writer.println();  // extra blank line for compile-safety
	}

	/**
	 * Translates one abstract TestSequence into a JUnit test-method.  Each
	 * test-method is styled "void testN() {...}", where N is the index of the
	 * TestSequence in the TestSuite.  Each method is prefixed by "@Test" and 
	 * has a body consisting of set-up and evaluation test steps.  Thereafter,
	 * visits each TestStep to generate the method body.
	 */
	@Override
	public void groundTestSequence(TestSequence sequence) {
		writeMethodComment(sequence);
		writer.println("\t@Test");
		writer.println("\tpublic void test" + testIndex + " () {");
		// The nested loop over test steps
		stepIndex = 0;  // first stepIndex == 0
		for (TestStep testStep : sequence.getTestSteps()) {
			testStep.receive(this);
			++stepIndex;
		}
		writer.println("\t}");
	}

	/**
	 * Translates one abstract TestStep into Java set-up and evaluation code.
	 * The first TestStep in a TestSequence translates into a creation step, 
	 * which instantiates the SUT.  Subsequent TestSteps translate into method
	 * calls on the SUT's interface.  Methods that return results store these
	 * in variables of the given types, which are named according to the names
	 * of the Outputs, in the style "outputN", where N is the index of the
	 * TestStep in the TestSequence.  If a TestStep is validated, generates
	 * code to compare the actual result(s) with the expected result(s) and
	 * invokes additional methods to query the state of the metadata.
	 */
	@Override
	public void groundTestStep(TestStep testStep) {
		if (testStep.isInitial())
			// Do nothing else here
			;
		else if (testStep.isFailure())
			writeFailureTestStep(testStep);
		else 
			writeNormalTestStep(testStep);
		if (testStep.isVerify() && metaCheck) {
			writeMetaDataAssertions(testStep);
		}
	}

	/**
	 * Prints out the private attributes used by the JUnit Test Driver class.
	 * These field declarations provide hooks to access the RESTful service and optionally a
	 * JavaFactory, if the TestSuite requires it.
	 * @param testSuite the TestSuite.
	 */
	protected void writeDriverFields(TestSuite testSuite) {
		writer.println();
		writer.println("\t/**");
		writer.println("\t * The root URI leading to the Service-Under-Test.");
		writer.println("\t */");
		writer.println("\tprivate final String REST_URI =");
		writer.print("\t\t\"" + endpoint);
		if (endpoint.endsWith("/"))
			writer.println("\";");   // URI already ends with forward slash
		else
			writer.println("/\";");  // URI needs extra forward slash
		writer.println();
		writer.println("\t/**");
		writer.println("\t * The JAX-RS interface to the Service-Under-Test.");
		writer.println("\t */");
		writer.println("\tprivate WebResource system = null;");
		writer.println();
		writer.println("\t/**");
		writer.println("\t * The JAX-RS implementation of the HTTP Client.");
		writer.println("\t */");
		writer.println("\tprivate ApacheHttpClient client = null;");
		writer.println();
		writer.println("\t/**");
		writer.println("\t * The Gson decoder for JSON-encoded strings.");
		writer.println("\t */");
		writer.println("\tprivate Gson decoder;");	
		
// The JAX-RS grounding only needs to use the JavaFactory if complex outputs
// must be synthesised before comparison against actual outputs decoded from
// JSON, because model inputs translate directly into path param strings,
// using their natural Java toString() representation.
		if (dependency.hasFactoryOutputs()) {
			writer.println();
			writer.println("\t/**");
			writer.println("\t * JavaFactory to synthesise objects of complex types.");
			writer.println("\t */");
			writer.println("\tprivate JavaFactory factory;");
		}
	}
	
	/**
	 * Prints out the test driver's constructor, with or without the Factory.
	 * If the JUnit test driver class needs the JavaFactory, adds extra
	 * creation and initialisation instructions in the constructor.
	 */
	@Override
	protected void writeDriverConstructor(TestSuite testSuite) {
		String driver = testSuite.getTestDriver();
		writer.println();
		writer.println("\t/**");
		writer.println("\t * Creates the JUnit test driver: " + driver + ".");
		writer.println("\t */");
		writer.println("\tpublic " + driver + "() {");
		// Create the Gson decoder.
		writer.println("\t\tdecoder = new Gson();  // The JSON decoder");
		
// The JAX-RS grounding only needs to use the JavaFactory if complex outputs
// must be synthesised before comparison against actual outputs decoded from
// JSON, because model inputs translate directly into path param strings,
// using their natural Java toString() representation.
		if (dependency.hasFactoryOutputs()) {
			writer.println("\t\tfactory = new JavaFactory();");
			for(String packageInfo : dependency.getSourcePackages()) {
				writer.println("\t\tfactory.useSourcePackage(\"" 
						+ packageInfo + "\");");
			}
		}

		writer.println("\t}");			
	}

	/**
	 * Prints out the set-up method for creating or reseting the System-Under-Test.   
	 * This is a @Before method that resets the RESTful client before each test 
	 * method.
	 * @param testSuite the test suite.
	 */
	protected void writeSystemSetUp(TestSuite testSuite) {
		writer.println();
		writer.println("\t/**");
		writer.println("\t * Creates the resources for the JAX-RS service before the first ");
		writer.println("\t * test; and resets the service before every test.  Initially, ");
		writer.println("\t * creates the configuration and tells this to handle state using");
		writer.println("\t * cookies.  Then, creates the HTTP client implementation.  Then");
		writer.println("\t * creates the service handle for the web resource and tells this");
		writer.println("\t * to accept the JSON media type.  Resets the service.");
		writer.println("\t */");		
		writer.println("\t@Before");
		writer.println("\tpublic void setUp() {");
		writer.println("\t\tif (system == null) {");
		writer.println("\t\t\tDefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();");
		writer.println("\t\t\tassertNotNull(\"Configuration is null!\", config);");
		writer.println("\t\t\tconfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);");
		writer.println("\t\t\tclient = ApacheHttpClient.create(config);");
		writer.println("\t\t\tassertNotNull(\"Apache Client is null!\", client);");
		writer.println("\t\t\tsystem = client.resource(REST_URI).path(\"reset\");");
		writer.println("\t\t\tassertNotNull(\"Service interface is null!\", system);");
		writer.println("\t\t\tsystem.accept(\"application/json\");");
		writer.println("\t\t}");
		writer.println("\t\tcallMethod(\"reset/\");");
		writer.println("\t}");
	}

	/**
	 * Prints out an extra secret method used by the Test Driver to build the
	 * complete URI path for the REST call, make the REST call and extract the
	 * JSON String response.  This simplifies the writing of individual tests.
	 */
	protected void writeCallMethod() {
		writer.println();
		writer.println("\t/**");
		writer.println("\t * Builds the complete URI for the REST call and extracts the JSON");
		writer.println("\t * Response.  This method is a utility used by the Test Driver to ");
		writer.println("\t * simplify making REST calls in each generated test.  It makes each");
		writer.println("\t * normal service call using POST and each inspection using GET and");
		writer.println("\t * returns the JSON String response, or null if none.  If the HTTP");
		writer.println("\t * status code indicates an error, throws a RuntimeException with an");
		writer.println("\t * explanation of the status, and a service error message.");
		writer.println("\t * @param path the operation name plus URL encoded parameters.");
		writer.println("\t * @return the JSON response, or null if the response is void.");		
		writer.println("\t */");		
 		writer.println("\tprivate String callMethod(String path) {");
		writer.println("\t\tClientResponse clientResponse;");
		writer.println("\t\tString response = null;");
		writer.println("\t\tsystem = client.resource(REST_URI + path);");
		writer.println("\t\tif (path.contains(\"getState\") || path.contains(\"getScenario\"))");
		writer.println("\t\t\tclientResponse = system.get(ClientResponse.class);");
		writer.println("\t\telse");
		writer.println("\t\t\tclientResponse = system.post(ClientResponse.class);");
		writer.println("\t\tif (clientResponse.hasEntity()) ");
		writer.println("\t\t\tresponse = clientResponse.getEntity(String.class);");
		writer.println("\t\tint status = clientResponse.getStatus();");
		writer.println("\t\tif (status < 200 || status >= 300)");
		writer.println("\t\t\tthrow new RuntimeException(status + \" \" +");
		writer.println("\t\t\t\tclientResponse.getClientResponseStatus() + \": \" + response);");
		writer.println("\t\treturn response;");
		writer.println("\t}");
	}
	
	/**
	 * Prints out an extra couple of methods used to make status checks on
	 * the service.  These simplify the checking of which scenario executed
	 * and which state was reached.  Does nothing if metaCheck == false.
	 */
	protected void writeStatusMethods() {
		if (metaCheck) {
			writer.println();
			writer.println("\t/**");
			writer.println("\t * Returns the name of the last scenario executed on the service.");
			writer.println("\t * This method is a utility to simplify making observations about");
			writer.println("\t * the service's behaviour in test-mode.");
			writer.println("\t */");		
			writer.println("\tprivate String getScenario() {");
			writer.println("\t\tString response = callMethod(\"getScenario/\");");
			writer.println("\t\treturn decoder.fromJson(response, String.class);");
			writer.println("\t}");
			writer.println();
			writer.println("\t/**");
			writer.println("\t * Returns the name of the last state visited by the service.");		
			writer.println("\t * This method is a utility to simplify making observations about");
			writer.println("\t * the service's behaviour in test-mode.");
			writer.println("\t */");		
			writer.println("\tprivate String getState() {");
			writer.println("\t\tString response = callMethod(\"getState/\");");
			writer.println("\t\treturn decoder.fromJson(response, String.class);");
			writer.println("\t}");
		}
	}
	
	/**
	 * Prints out package information at the head of the generated JUnit test
	 * driver class source file.  If a target package was set, prints this
	 * out first; then prints the standard JUnit imports; then prints out any
	 * other Java package imports. 
	 * @param testSuite the TestSuite.
	 */
	protected void writePackageInfo(TestSuite testSuite) {
		String driverPackage = dependency.getTargetPackage();
		if (! driverPackage.isEmpty()) {
			writer.println("package " + driverPackage + ";");
			writer.println();
		}
		// JUnit 4 imports
		writer.println("import org.junit.*;");
		writer.println("import static org.junit.Assert.*;");
		writer.println();
		// Google Gson imports
		writer.println("import com.google.gson.Gson;");
		if (dependency.hasGenericOutputs())
			writer.println("import com.google.gson.reflect.TypeToken;");
		writer.println();
		// Apache Jersey imports
		writer.println("import com.sun.jersey.api.client.ClientResponse;");   
		writer.println("import com.sun.jersey.api.client.WebResource;");   
		writer.println("import com.sun.jersey.client.apache.ApacheHttpClient;");   
		writer.println("import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;");   
		writer.println("import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;");   		
		writer.println();
		// End-user type imports only needed for outputs
		if (dependency.hasFactoryOutputs()) {
			for (String importInfo : dependency.getImports()) {
				writer.println("import " + importInfo + ";");
			}
		}
	}
	
	/**
	 * Prints out a class-level comment for the JUnit test driver class 
	 * source file.  This includes the name of the test driver and the
	 * date it was created.  Prints out the test conditions under which
	 * the file was generated.
	 * @param testSuite the TestSuite.
	 */
	protected void writeClassComment(TestSuite testSuite) {
		String system = testSuite.getSystem();
		String driver = testSuite.getTestDriver();
		writer.println();
		writer.println("/**");
		writer.println(" * " + driver + " generated on " + new Date());
		writer.print(" * by Broker@Cloud generator");
		writer.println(" uk.ac.sheffield.vtts.ground.JaxRsGrounding");
		writer.println(" *");
		writer.print(" * System Under Test (SUT) is the JAX-RS RESTful service: ");
		writer.println(system);
		writer.println(" * accessed through the public URI:");
		writer.println(" *");
		writer.println(" *\t" + endpoint);
		writer.println(" *");		
		writer.println(" * We assume that @PathParam conventions are used by the server to convert");
		writer.println(" * all arguments supplied in the path back to suitable objects; typically");
		writer.println(" * each type must provide a constructor accepting a String.  We also assume");
		writer.println(" * that the service returns responses encoded as JSON strings.  This REST");
		writer.println(" * client uses Google's Gson to convert JSON to suitable objects.  If the");
		writer.println(" * service returns multiple values in any response, these must be returned");
		writer.println(" * as a JSON array of objects having the same type - this is a limitation");
		writer.println(" * of the Gson decoder.  If the REST server returns a status code outside of");
		writer.println(" * the 200-series (indicating OK), this client raises an explicit exception,");
		writer.println(" * whose message is checked.  The REST service must also offer an explicit");
		writer.println(" * reset/ operation to put it in a clean initial state.");
		writer.println(" *");
		Notice notice = testSuite.getNotice();
		for (Notice analysis : notice.getNotices()) {
			writer.println(" *\t\t" + analysis.getText());
		}
		writer.println(" */");
	}
	
	/**
	 * Optionally prints out an assertion check on the result of the set-up
	 * code that created the service.  This should only be done once, if this
	 * TestStep is to be verified.
	 * @param testStep the TestStep.
	 */
	protected void writeInitialTestStep(TestStep testStep) {
		if (testStep.isVerify()) {
			writer.println("\t\t// Verify service reset step #0");
			writer.println("\t\tassertNotNull(system);");	
		}
	}
	
	/**
	 * Prints out a method invocation expecting a normal return value.  If
	 * this TestStep is to be verified, prints out variables to receive the
	 * return value(s) and adds assertion checks to compare expected with
	 * actual results.
	 * @param testStep the TestStep to translate.
	 */
	protected void writeNormalTestStep(TestStep testStep) {
		Operation operation = testStep.getOperation();		
		if (testStep.isVerify()) {
			// Here is where we declare the JSON response string.
			writer.print("\t\tString response" + stepIndex);
			writer.print(" = callMethod(\"" + operation.getName() + "/");
			writeInputList(operation.getInputs());	// URLEncode the inputs
			writer.println("\");");
			// Here is where we decode the JSON string back to Java.
			writeResultVariables(operation.getOutputs());
			writer.println("\t\t// Verify invocation step #" + stepIndex);
			writeResultAssertions(operation.getOutputs());
		}
		else
		{
			writer.print("\t\tcallMethod(\"" + operation.getName() + "/");
			writeInputList(operation.getInputs());  // URLEncode the inputs
			writer.println("\");");
		}
			
	}

	/**
	 * Prints out a method invocation expecting to throw an exception.  If
	 * this TestStep is to be verified, inserts an automatic JUnit fail()
	 * assertion after the tested method, which causes this test to fail,
	 * if the method was executed without throwing an exception.
	 * @param testStep the TestStep.
	 */
	@Override
	protected void writeFailureTestStep(TestStep testStep) {
		Operation operation = testStep.getOperation();
		Failure fail = operation.getFailures().iterator().next();
		writer.println("\t\ttry {");
		writer.print("\t\t\tcallMethod(\"" + operation.getName() + "/");
		writeInputList(operation.getInputs());	
		writer.println("\");");
		if (testStep.isVerify()) {
			writer.println("\t\t\t// Verify exception step #" + stepIndex);
			writer.println("\t\t\tfail(\"Expected an exception: " 
					+ fail.getContent() + "\");");
		}
		writer.println("\t\t}");
		writer.println("\t\tcatch (Exception ex) {");
		if (testStep.isVerify()) 
			writer.println("\t\t\tassertTrue(ex.getMessage().contains(\"" 
					+ fail.getContent() + "\"));");
		else
			writer.println("\t\t\t// Exception has already been verified.");
		writer.println("\t\t}");
	}
	
	/**
	 * Tells this Grounding to use the given root URI as the endpoint.
	 * @param serviceURI the root URI of the service.
	 */
	@Override
	public void useEndpoint(String serviceURI) {
		this.endpoint = serviceURI;
	}

	/**
	 * Prints the list of argument value(s) for the operation inputs, in the format 
	 * needed for building the URI path for the RESTful service.
	 * e.g.  + param1 + "/" + param2 + "/" 
	 * @param inputs the ordered set of inputs.
	 */
	@Override
	protected void writeInputList(Set<Input> inputs) {
		for(Input in : inputs) {
// Every input is guaranteed to be bound.  If the XML content is null, evaluate()
// will create a default value, such as zero, false, empty string, set, list, map.
			String content = in.evaluate().toString();
			String pathParam = content.replaceAll(" ", "%20");
			writer.print(pathParam + "/");
		}
	}
	
	/**
	 * Prints the appropriate kind of receiver variable(s) for the outputs,
	 * and initialises these to the result of decoding the JSON response.
	 * @param outputs the ordered set of outputs.
	 */
	@Override
	protected void writeResultVariables(Set<Output> outputs) {
		int outCount = outputs.size();
		if (outCount > 1) {
			Output out = outputs.iterator().next();			
			writer.print("\t\tObject[] actual" + stepIndex);
			writer.print(" = decoder.fromJson(response" + stepIndex + ", ");
			// Creates the JSON array type by guessing that all elements have the same type.
			writeArrayTypeToken(out);
			writer.println(");");
		}
		else if (outCount == 1) {
			Output out = outputs.iterator().next();
			String outName = out.getName();
		    writer.print("\t\t");
			writeJavaType(out); 
			writer.print(" " + outName + stepIndex);
			writer.print(" = decoder.fromJson(response" + stepIndex + ", ");
			writeTypeToken(out);
			writer.println(");");					
		}
	}
	
	/**
	 * Special assertion to check that an operation specified as returning 
	 * a void result actually returned nothing useful.
	 */
	@Override
	protected void writeVoidAssertion() {
		writer.println("\t\tassertNull(response" + stepIndex + ");");
	}

	/**
	 * Prints the JUnit assertions checking that the system executed the
	 * expected scenario in this TestStep and ended in the expected state.
	 * @param testStep the TestStep whose properties are to be asserted.
	 */
	@Override
	protected void writeMetaDataAssertions(TestStep testStep) {
		String state = testStep.getState();
		String branch = testStep.getName();
		// Splits off the [subscript] from ignore[subscript]
		int index = branch.indexOf('[');
		if (index != -1)
			branch = branch.substring(0, index);
		writer.println("\t\tassertEquals(\"" + branch + "\", getScenario());");
		writer.println("\t\tassertEquals(\"" + state + "\", getState());");
	}
	
	/**
	 * Generates the runtime type artifact needed by Gson to decode the JSON
	 * for a simple class, or a generic class.
	 * Either prints "Foo.class" for some simple class Foo, or prints a
	 * TypeToken expression for a generic class.
	 * @param output the model output type.
	 */
	private void writeTypeToken(Output output) {
		String type = output.getType();
		if (type.indexOf('[') + type.indexOf(']') > 0) {
			writer.write("new TypeToken<");
			writeJavaType(output);
			writer.write(">(){}.getType()");
		}
		else {
			writeJavaType(output);
			writer.write(".class");
		}
	}
	
	/**
	 * Generates the runtime type artifact needed by Gson to decode the JSON
	 * for a simple array, or a generic array.
	 * Either prints "Foo[].class" for some simple class Foo, or prints a
	 * TypeToken expression for an array whose element type is a generic 
	 * class.
	 * @param output the model output type.
	 */
	private void writeArrayTypeToken(Output output) {
		String type = output.getType();
		if (type.indexOf('[') + type.indexOf(']') > 0) {
			writer.write("new TypeToken<");
			writeJavaType(output);
			writer.write("[]>(){}.getType()");			
		}
		else {
			writeJavaType(output);
			writer.write("[].class");
		}
	}

}
