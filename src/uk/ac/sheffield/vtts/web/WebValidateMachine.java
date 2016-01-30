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

package uk.ac.sheffield.vtts.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jast.ast.ASTError;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;

import uk.ac.sheffield.vtts.model.SemanticError;
import uk.ac.sheffield.vtts.model.Service;

/**
 * WebValidateMachine is a CGI program to validate the state machine of a cloud
 * service specification.  This program expects to be launched by a CGI script
 * which, in turn, is the action performed by a web form.  The script merely
 * invokes the Java runtime with this class as the main program.  This program
 * decodes the web form data supplied as POST-data on standard input, and then
 * validates the state machine of the service referenced in the form.  The
 * output is an annotated XML file of the state machine.  
 * 
 * Requires ASTReader, ASTWriter, ASTError from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 1.0
 */
public class WebValidateMachine {

	/**
	 * Reads the URL-encoded POST-data on standard input, decodes this as a
	 * Java map containing the key "url" associated with a value, the URL of
	 * the specification file.  Reads the specification file, which is in the
	 * Broker@Cloud XML specification format conforming to ServiceSchema.xsd,
	 * and builds a model of the specification in memory.  Validates the state
	 * machine of the specification, then outputs an annotated XML file,
	 * issuing warnings about any unreachable states; and also providing an
	 * analysis of missing transitions per state. 
	 * @param args empty, since no command-line parameters are passed.
	 */
	public static void main(String[] args) {
		BufferedReader input = null;
		String postData = null;
		Map<String, String> form = new LinkedHashMap<String, String>();
		try {
			input = new BufferedReader(new InputStreamReader(System.in));
			postData = URLDecoder.decode(input.readLine(), "UTF-8");
		    for (String entry : postData.split("&")) {
		        int pos = entry.indexOf("=");
		        if (pos != -1) {
		          String key = entry.substring(0, pos);
		          String val = entry.substring(pos + 1);
		          form.put(key, val);
		        }
		        else 
		        	throw new IOException("Badly formatted web form data");
		    }
		    URL url = new URL(form.get("url"));
		    ASTReader reader = new ASTReader(url, "UTF-8");
			reader.usePackage("uk.ac.sheffield.vtts.model");
			Service service = (Service) reader.readDocument();
			reader.close();
			
			service.validateMachine();
			
			PrintWriter printer = new PrintWriter(System.out);
			ASTWriter writer = new ASTWriter(printer, "UTF-8");
			writer.usePackage("uk.ac.sheffield.vtts.model");
	        printer.println("Content-Type: text/xml\n");
			writer.writeDocument(service.getMachine());
			writer.close();
			
		}
		catch (ASTError ex) {
			syntaxError(ex);
		}
		catch (SemanticError ex) {
			semanticError(ex);
		}
		catch (Throwable ex) {
			serviceError(ex);
		}
	}
	
	  /**
	   * Prints a validation error text in HTML.
	   */
	  private static void syntaxError(ASTError ex) {
	        System.out.println("Content-Type: text/html\n");
	        System.out.println("<html><head>");
	       System.out.println("<title>Syntax Error</title>");
	        System.out.println("</head><body>");
	        System.out.println("<h2>Syntax Error</h2>");
	        System.out.println("<p>" + ex.getMessage() + "</p>");
	        System.out.println("<p>The web service determined that the" +
	        		" input specification was invalid.  Please correct" +
	        		" the input specification and resubmit it.</p>");
	        System.out.println("<p>Further diagnostic information is given" +
	        		" below as a stack backtrace.");
	        System.out.println("<pre>");
	        ex.printStackTrace();
	        System.out.println("</pre>");	        
	        System.out.println("</body></html>");
	  }

	  /**
	   * Prints a service error text in HTML.
	   */
	  private static void semanticError(SemanticError ex) {
		  System.out.println("Content-Type: text/html\n");
	        System.out.println("<html><head>");
	        System.out.println("<title>Semantic Error</title>");
	        System.out.println("</head><body>");
	        System.out.println("<h2>Semantic Error</h2>");
	        System.out.println("<p>" + ex.getMessage() + "</p>");
	        System.out.println("<p>The web service determined that the" +
	        		" input specification was invalid.  Please correct" +
	        		" the input specification and resubmit it.</p>");
	        System.out.println("<p>Further diagnostic information is given" +
	        		" below as a stack backtrace.");
	        System.out.println("<pre>");
	        ex.printStackTrace();
	        System.out.println("</pre>");	        
	        System.out.println("</body></html>");
	  }
	  
	  /**
	   * Prints a service error text in HTML.
	   */
	  private static void serviceError(Throwable ex) {
	        System.out.println("Content-Type: text/html\n");
	        System.out.println("<html><head>");
	        System.out.println("<title>Service Error</title>");
	        System.out.println("</head><body>");
	        System.out.println("<h2>Service Error</h2>");
	        System.out.println("<p>The web service failed for some general" +
	        		" reason while processing your request.  Further diagnostic" +
	        		" information is given below as a stack backtrace.</p>");
	        System.out.println("<pre>");
	        ex.printStackTrace();
	        System.out.println("</pre>");
	        System.out.println("</body></html>");
	  }

}
