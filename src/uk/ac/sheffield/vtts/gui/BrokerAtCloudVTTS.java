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

package uk.ac.sheffield.vtts.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import jsyntaxpane.DefaultSyntaxKit;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JSplitPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import uk.ac.sheffield.vtts.ground.Grounding;
import uk.ac.sheffield.vtts.ground.JavaGrounding;
import uk.ac.sheffield.vtts.ground.JaxRsGrounding;
import uk.ac.sheffield.vtts.ground.JaxWsGrounding;
import uk.ac.sheffield.vtts.model.Service;
import uk.ac.sheffield.vtts.model.TestSuite;

import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;
import org.jast.ast.NodeError;

import javax.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.Component;

/**
 * BrokerAtCloudVTTS is a Java Swing GUI that demonstrates the verification
 * and testing tools.  BrokerAtCloudVTTS can be launched as a stand-alone
 * program (from the command line; or by double-clicking on an executable
 * jar-file icon).  It allows users to interact with different tools from
 * the Broker@Cloud Verification and Testing Tool Suite, including the tools
 * for Validation, Verification, Test Generation and Test Grounding.  Each
 * tool is launched from a separate tabbed pane in the GUI, and provides 
 * suitable buttons and fields to load, process and save XML files, which 
 * are stored in the default package structure of this software distribution,
 * unless you indicate otherwise.  Please note that whereas the first three 
 * tabs load an XML service specification; the Test Grounding tab loads a 
 * (previously-generated) XML test suite.
 * <p>
 * The BrokerAtCloudVTTS GUI requires the DefaultSyntaxKit class from 
 * Google's <code>jsyntaxpane</code> library in order to display formatted 
 * XML and Java code with syntax highlighting.  However, the  highlighter 
 * can slow down processing, so we provde an option to disable syntax 
 * highlighting, when rendering particularly long test suites.
 * 
 * @author Raluca Lefticaru
 * @version Broker@Cloud 1.0
 */
public class BrokerAtCloudVTTS extends JFrame {

	/**
	 * Automatically generated serialVersionUID
	 */
	private static final long serialVersionUID = 1041224917127330422L;

	/**
	 * Path to the Broker@Cloud icon image.
	 */
	private static final String iconPath = "/uk/ac/sheffield/vtts/gui/BrokerAtCloud_small.png";
	private static final String iconWaitingPath = "/uk/ac/sheffield/vtts/gui/progressbar.gif";
	
	private JPanel contentPane;
	private JTextField tf_SpecificationFilePath4ValidationTab;
	private JTextField tf_SpecificationFilePath4VerificationTab;
	private JTextField tf_SpecificationFilePath4TestGenerationTab;
	private JEditorPane editorPaneSpec4ValidationTab;
	private JEditorPane editorPane_ValidationReport;
	private JTextField textField_TestFilePath_GroundingTab;
	private JTextField textField_RemoteServiceBase;
	private JTextField textField_TestDriver;

	private JEditorPane editorPaneSpec4VerificationTab;
	private JEditorPane editorPane_VerificationReport;
	private JEditorPane editorPaneSpec4TestGenerationTab;
	private JEditorPane editorPane_TestGenerationOutputFile;
	private JEditorPane editorPaneGrounding_InputFile;
	private JEditorPane editorPaneGrounding_JUnitOutputFile;

	// Fixed by adding suitable actual type String
	private JComboBox<String> comboBox_grounding;
	private JCheckBox chckbxVerifyTestsTransitions;

	private JSpinner spinner4TestDepth;
	private JCheckBox chckbxMultiobjectiveTests;
	private JTextField tf_ServiceClientSourcePackages;

	private JButton btnCheckStateMachine;
	private JButton btnCheckProtocol;
	private JButton btnGenerateJunitTests;
	private JButton btn_OpenSpecificationFile;
	private JButton btnGenerateTests;

	private JLabel lbValidationOutputFile;
	private JLabel lbVerificationOutputFile;
	private JPanel testGenerationPanel;
	private JLabel lbAbstractTestsFile;
	private JLabel lbJUnitFile;
	private JCheckBox chckbxPrettyFormattingTests;
	private JCheckBox chckbxPrettyFormattingJUnit;
	
	private JLabel label_waiting4TestGrounding;
	private JLabel lbPleaseWait4TestGrounding;
	private JPanel panel_1;
	private JLabel label_waiting4TestGeneration;
	private JLabel lbPleaseWait4TestGeneration;

	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	private void doOpenSpecificationFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"XML FILES", "XML");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setDialogTitle("Open Specification File");
		File workingDirectory = new File(System.getProperty("user.dir"));
		fileChooser.setCurrentDirectory(workingDirectory);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			// load content from file and display in all 3 editor panes

			File file = fileChooser.getSelectedFile();
			tf_SpecificationFilePath4ValidationTab.setText(file
					.getAbsolutePath());
			tf_SpecificationFilePath4VerificationTab.setText(file
					.getAbsolutePath());
			tf_SpecificationFilePath4TestGenerationTab.setText(file
					.getAbsolutePath());

			DefaultSyntaxKit.initKit();
			editorPaneSpec4ValidationTab.setContentType("text/xml");
			editorPaneSpec4VerificationTab.setContentType("text/xml");
			editorPaneSpec4TestGenerationTab.setContentType("text/xml");

			try {
				String content = readFile(file.getAbsolutePath(),
						Charset.defaultCharset());
				editorPaneSpec4ValidationTab.setText(content);
				editorPaneSpec4VerificationTab.setText(content);
				editorPaneSpec4TestGenerationTab.setText(content);
				editorPane_ValidationReport.setText("");
				editorPane_VerificationReport.setText("");
				editorPane_TestGenerationOutputFile.setText("");				
				lbValidationOutputFile.setText("");
				lbVerificationOutputFile.setText("");
				lbAbstractTestsFile.setText("");
				btnCheckStateMachine.setEnabled(true);
				btnCheckProtocol.setEnabled(true);
				btnGenerateTests.setEnabled(true);
			} catch (IOException ioEx) {
				JOptionPane.showMessageDialog(
						this,
						"An exception occurred trying to read this file "
								+ file.getAbsolutePath() + " : "
								+ ioEx.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				ioEx.printStackTrace();
				btnCheckStateMachine.setEnabled(false);
				btnCheckProtocol.setEnabled(false);
				btnGenerateTests.setEnabled(false);
				editorPaneSpec4ValidationTab.setText("");
				editorPaneSpec4VerificationTab.setText("");
				editorPaneSpec4TestGenerationTab.setText("");
				editorPane_ValidationReport.setText("");
				editorPane_VerificationReport.setText("");
				editorPane_TestGenerationOutputFile.setText("");
				lbValidationOutputFile.setText("");
				lbVerificationOutputFile.setText("");
				lbAbstractTestsFile.setText("");
				tf_SpecificationFilePath4ValidationTab.setText("");
				tf_SpecificationFilePath4VerificationTab.setText("");
				tf_SpecificationFilePath4TestGenerationTab.setText("");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "An exception occurred: "
						+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
				btnCheckStateMachine.setEnabled(false);
				btnCheckProtocol.setEnabled(false);
				btnGenerateTests.setEnabled(false);
				editorPaneSpec4ValidationTab.setText("");
				editorPaneSpec4VerificationTab.setText("");
				editorPaneSpec4TestGenerationTab.setText("");
				editorPane_ValidationReport.setText("");
				editorPane_VerificationReport.setText("");
				editorPane_TestGenerationOutputFile.setText("");
				lbAbstractTestsFile.setText("");
				tf_SpecificationFilePath4ValidationTab.setText("");
				tf_SpecificationFilePath4VerificationTab.setText("");
				tf_SpecificationFilePath4TestGenerationTab.setText("");
			
			}
		}

	}

	private void doOpenTestFile_TestGroundingTab() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"XML FILES", "XML");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setDialogTitle("Open Abstract Test Suite File");
		File workingDirectory = new File(System.getProperty("user.dir"));
		fileChooser.setCurrentDirectory(workingDirectory);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			textField_TestFilePath_GroundingTab.setText(file.getAbsolutePath());
			editorPaneGrounding_JUnitOutputFile.setText("");
			lbJUnitFile.setText("");
			// load from file and display in editor pane
			label_waiting4TestGrounding.setVisible(true);
			lbPleaseWait4TestGrounding.setVisible(true);
			if (chckbxPrettyFormattingJUnit.isSelected())
				{
				DefaultSyntaxKit.initKit();
				editorPaneGrounding_InputFile.setContentType("text/xml");
				editorPaneGrounding_JUnitOutputFile.setContentType("text/xml");
				}
			else 
			{
				editorPaneGrounding_InputFile.setContentType("");
				editorPaneGrounding_JUnitOutputFile.setContentType("");				
			}
			final String inputAbstractTestSuiteFile = file.getAbsolutePath();
			try {
				//String content = readFile(file.getAbsolutePath(),
					//	Charset.defaultCharset());

				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							FileReader fileReader = new FileReader(inputAbstractTestSuiteFile);
							BufferedReader bufReader = new BufferedReader(
									fileReader);
							editorPaneGrounding_InputFile.read(bufReader, "");

						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null, "An exception occurred: "
									+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							editorPaneGrounding_InputFile.setText("");
							btnGenerateJunitTests.setEnabled(false);
							textField_TestFilePath_GroundingTab.setText("");
						}
						label_waiting4TestGrounding.setVisible(false);
						lbPleaseWait4TestGrounding.setVisible(false);
					}
				});
				thread.start();				
				btnGenerateJunitTests.setEnabled(true);
			} 
			 catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "An exception occurred: "
						+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
				editorPaneGrounding_InputFile.setText("");
				btnGenerateJunitTests.setEnabled(false);
				textField_TestFilePath_GroundingTab.setText("");
			}
		}
	}

	private Service readSpecification(String path) {
		File inputFile = new File(path);

		ASTReader reader = null;
		Service service = null;

		try {
			reader = new ASTReader(inputFile);
			reader.usePackage("uk.ac.sheffield.vtts.model");
			service = (Service) reader.readDocument();
			if (reader != null)
				reader.close();
			System.out
					.println("Unmarshalled the specification from input file: "
							+ inputFile);
		} catch (ClassCastException ex) {
			JOptionPane.showMessageDialog(this,
					"XML file must contain root element: Service ", "Error",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			label_waiting4TestGeneration.setVisible(false);
			lbPleaseWait4TestGeneration.setVisible(false);
			lbAbstractTestsFile.setText("");
			editorPane_TestGenerationOutputFile.setText("");
			throw new NodeError("XML file must contain root element: Service");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
					"An exeption occurred:  " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		} finally {
			// if (reader != null) reader.close();
			reader = null;
		}

		return service;

	}

	private void doCheckStateMachine(String path) {
		System.out.println("Starting machine check.\n");

		File inputFile = new File(path);
		File directory = inputFile.getParentFile();

		Service service = readSpecification(path);

		String outputName = service.getName() + "Validation.xml";
		File outputFile = new File(directory, outputName);

		service.validateMachine();

		ASTWriter writer = null;
		try {
			writer = new ASTWriter(outputFile);
			writer.usePackage("uk.ac.sheffield.vtts.model");
			writer.writeDocument(service.getMachine());
			if (writer != null)
				writer.close();
			System.out.println("Marshalled the validation to output file: "
					+ outputFile);
			System.out.println("\nState machine check completed with success.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"An exeption occurred:  " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		try {
			String content = readFile(outputFile.getAbsolutePath(),
					Charset.defaultCharset());
			DefaultSyntaxKit.initKit();
			editorPane_ValidationReport.setContentType("text/xml");
			editorPane_ValidationReport.setText(content);
			lbValidationOutputFile.setText(outputFile.getAbsolutePath());
		} catch (IOException ioEx) {
			JOptionPane.showMessageDialog(this, "An exception occurred: "
					+ ioEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			ioEx.printStackTrace();
		}

	}

	private void doCheckProtocol(String path) {
		System.out.println("Starting protocol check.\n");

		File inputFile = new File(path);
		File directory = inputFile.getParentFile();

		Service service = readSpecification(path);

		String outputName = service.getName() + "Verification.xml";
		File outputFile = new File(directory, outputName);

		service.verifyProtocol();

		ASTWriter writer = null;

		try {
			writer = new ASTWriter(outputFile);
			writer.usePackage("uk.ac.sheffield.vtts.model");
			writer.writeDocument(service.getProtocol());
			if (writer != null)
				writer.close();
			System.out.println("Marshalled the verification to output file: "
					+ outputFile);
			System.out.println("\nProtocol check completed with success.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"An exeption occurred:  " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		try {
			String content = readFile(outputFile.getAbsolutePath(),
					Charset.defaultCharset());
			DefaultSyntaxKit.initKit();
			editorPane_VerificationReport.setContentType("text/xml");
			editorPane_VerificationReport.setText(content);
			lbVerificationOutputFile.setText(outputFile.getAbsolutePath());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
					"An exception occurred: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			ex.getStackTrace();
		}

	}

	private void doGenerateTests(String path, Integer test_depth,
			Boolean multiobjectiveTestFlag) {		
		
		System.out.println("Starting abstract test generation.\n");
		File inputFile = new File(path);
		File directory = inputFile.getParentFile();
		
	
		try {

			Service service = readSpecification(path);
			
			String outputName = service.getName() + "Tests.xml";
			File outputFile = new File(directory, outputName);

			int testDepth = service.getTestDepth(); // if specified
			boolean multiTest = service.isMultiTest(); // if specified

			testDepth = test_depth;

			multiTest = multiobjectiveTestFlag;

			TestSuite testSuite = service.generateTests(testDepth, multiTest);

			ASTWriter writer = new ASTWriter(outputFile);
			writer.writeDocument(testSuite);
			writer.close();
			//String content = readFile(outputFile.getAbsolutePath(),
			//		Charset.defaultCharset());
			
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufReader = new BufferedReader(fileReader);

			lbAbstractTestsFile.setText(outputFile.getAbsolutePath());
			if (chckbxPrettyFormattingTests.isSelected()) {
				DefaultSyntaxKit.initKit();
				try {
					Thread.sleep(400);
					editorPane_TestGenerationOutputFile
							.setContentType("text/xml");

				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "An exception occurred: "
							+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			} else {
				try {
					editorPane_TestGenerationOutputFile.setContentType("");
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "An exception occurred: "
							+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
			
			//editorPane_TestGenerationOutputFile.setText(content);
			editorPane_TestGenerationOutputFile.read(bufReader, "");
			System.out.println("Marshalled the test suite to output file: "
					+ outputFile);
			System.out.println("\nTest generation completed with success.");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
					"An exception occurred: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			ex.getStackTrace();

 		}
		
	}

	private void doTestGrounding(String path, String groundingParam,
			Boolean metaCheckParam, String remoteServiceBaseURLParam,
			String targetPackageParam, String sourcePackagesParam) {
		String DEFAULT_REST_URI = "http://my.rest.server";
		String TEST_PACKAGE_PATH = "uk.ac.sheffield.vtts.test";
		String CLIENT_PACKAGE_PATH = "uk.ac.sheffield.vtts.client";
		String SOURCE_CODE_ROOT = "src";
		System.out.println("Starting tests grounding.\n");
		try {
			File inputFile = new File(path);

			ASTReader reader = null;
			TestSuite testSuite = null;
			try {
				reader = new ASTReader(inputFile);
				reader.usePackage("uk.ac.sheffield.vtts.model"); // use model classes from here
				testSuite = (TestSuite) reader.readDocument();
			} catch (ClassCastException ex) {
				JOptionPane
						.showMessageDialog(
								this,
								"The XML file must contain root element: TestSuite",
								"Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
				label_waiting4TestGrounding.setVisible(false);
				lbPleaseWait4TestGrounding.setVisible(false);
				lbJUnitFile.setText("");
				editorPaneGrounding_JUnitOutputFile.setText("");
				throw new NodeError(
						"XML file must contain root element: TestSuite");
			} finally {
				if (reader != null)
					reader.close();
			}

			System.out.println("Unmarshalled the test suite from input file: "
					+ inputFile);

			// Default values for all the generation parameters
			String grounding = testSuite.getGrounding();
			boolean metaCheck = testSuite.getMetaCheck();
			String targetPackage = null;
			String endpointUri = null;
			List<String> sourcePackages = new ArrayList<String>();

			grounding = groundingParam; 
			metaCheck = metaCheckParam;

			if ( remoteServiceBaseURLParam != null)
				if (!remoteServiceBaseURLParam.trim().equals("")
					&& remoteServiceBaseURLParam.startsWith("http")) {
				endpointUri = remoteServiceBaseURLParam;
			}

			if ( targetPackageParam != null)
				if (!targetPackageParam.trim().equals("")) {
					targetPackage = targetPackageParam;
			}

			if (sourcePackagesParam != null)
				if (!sourcePackagesParam.trim().equals("")) {
					sourcePackages.addAll(Arrays.asList(sourcePackagesParam
						.split("\\s+")));
			}

			// If none were supplied, use default source and target packages
			if (targetPackage == null || sourcePackages.isEmpty()) {
				String pkgExt;  // package extension for locating generated code
				if (grounding.equals("JAX-WS"))
					pkgExt = ".ws";
				else if (grounding.equals("JAX-RS"))
					pkgExt = ".rs";
				else
					pkgExt = ".pojo";
				if (targetPackage == null)
					targetPackage = TEST_PACKAGE_PATH + pkgExt;  // Test driver package
				if (sourcePackages.isEmpty())
					sourcePackages.add(CLIENT_PACKAGE_PATH + pkgExt); // Java client package
			}

			if (endpointUri == null)
				endpointUri = DEFAULT_REST_URI;

			File directory = new File(new File(SOURCE_CODE_ROOT),
					targetPackage.replace('.', File.separatorChar));

			String outputName = testSuite.getTestDriver() + ".java";
			File outputFile = new File(directory, outputName);

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new FileWriter(outputFile), true);
				Grounding visitor = null;
				if (grounding.equals("Java"))
					visitor = new JavaGrounding(writer);
				else if (grounding.equals("JAX-WS"))
					visitor = new JaxWsGrounding(writer);
				else if (grounding.equals("JAX-RS"))
					visitor = new JaxRsGrounding(writer);
				else
					JOptionPane.showMessageDialog(this,
							"The grounding type can be one of the following: Java | JAX-WS | JAX-RS" ,
							"Error", JOptionPane.ERROR_MESSAGE);

				visitor.setMetaCheck(metaCheck);
				visitor.useEndpoint(endpointUri);
				visitor.useTargetPackage(targetPackage);
				for (String sourcePackage : sourcePackages)
					visitor.useSourcePackage(sourcePackage);

				testSuite.receive(visitor);
			} finally {
				if (writer != null)
					writer.close();
			}

			//String content = readFile(outputFile.getAbsolutePath(),
			//		Charset.defaultCharset());
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufReader = new BufferedReader(fileReader);
			
			if (chckbxPrettyFormattingJUnit.isSelected()) {
				DefaultSyntaxKit.initKit();
				try {
					Thread.sleep(400);
					editorPaneGrounding_JUnitOutputFile
							.setContentType("text/java");
				} catch (Exception e) {
					//System.out.println("Exception:  " + e.getMessage());
					JOptionPane.showMessageDialog(this, "An exception occurred: "
							+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			} else {
				try {
					editorPaneGrounding_JUnitOutputFile.setContentType("");
				} catch (Exception e) {
					//System.out.println("Exception:  " + e.getMessage());
					JOptionPane.showMessageDialog(this, "An exception occurred: "
							+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
			lbJUnitFile.setText(outputFile.getAbsolutePath());
			
			//editorPaneGrounding_JUnitOutputFile.setText(content);
			editorPaneGrounding_JUnitOutputFile.read(bufReader, "");
			System.out.println("Generated Java tests written to output file: "
					+ outputFile);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
					"An exception occurred: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}

		System.out.println("\nGrounding completed with success.");

	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BrokerAtCloudVTTS frame = new BrokerAtCloudVTTS();
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					frame.setVisible(true);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,
							"An exception occurred: " + ex.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BrokerAtCloudVTTS() {
		setTitle("Broker@Cloud verification and testing tool suite");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 851, 540);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(
				Alignment.LEADING).addComponent(tabbedPane));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
				Alignment.LEADING).addComponent(tabbedPane));

		JPanel validationPanel = new JPanel();
		validationPanel.setToolTipText("Validation");
		tabbedPane.addTab("Validation", null, validationPanel, null);

		JLabel lbVerificationTabTitle = new JLabel(
				"<html><span style=\"color:blue; font-size:17px\">STATE MACHINE COMPLETENESS CHECKER</span></html>");

		btn_OpenSpecificationFile = new JButton("Specification");
		btn_OpenSpecificationFile
				.setToolTipText("Click to choose specification file");
		btn_OpenSpecificationFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// doOpenSpecificationFile_ValidationTab();
				doOpenSpecificationFile();
			}
		});

		tf_SpecificationFilePath4ValidationTab = new JTextField();
		tf_SpecificationFilePath4ValidationTab.setEditable(false);
		tf_SpecificationFilePath4ValidationTab.setColumns(10);

		btnCheckStateMachine = new JButton("Check State Machine");
		btnCheckStateMachine.setEnabled(false);
		btnCheckStateMachine.setToolTipText("Click to check completeness");
		btnCheckStateMachine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCheckStateMachine(tf_SpecificationFilePath4ValidationTab
						.getText().trim());
			}
		});

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.5);

		JScrollPane scrollPane_4 = new JScrollPane();
		splitPane_1.setLeftComponent(scrollPane_4);

		editorPaneSpec4ValidationTab = new JEditorPane();
		editorPaneSpec4ValidationTab.setEditable(false);
		scrollPane_4.setViewportView(editorPaneSpec4ValidationTab);

		JScrollPane scrollPane_5 = new JScrollPane();
		splitPane_1.setRightComponent(scrollPane_5);

		editorPane_ValidationReport = new JEditorPane();
		editorPane_ValidationReport.setEditable(false);
		scrollPane_5.setViewportView(editorPane_ValidationReport);

		JLabel lbLogo1 = new JLabel("");
		lbLogo1.setIcon(new ImageIcon(BrokerAtCloudVTTS.class
				.getResource(iconPath)));

		lbValidationOutputFile = new JLabel();
		lbValidationOutputFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lbValidationOutputFile.setHorizontalTextPosition(SwingConstants.RIGHT);
		lbValidationOutputFile.setMaximumSize(new Dimension(400, 14));
		lbValidationOutputFile.setForeground(Color.BLUE);
		GroupLayout gl_validationPanel = new GroupLayout(validationPanel);
		gl_validationPanel
				.setHorizontalGroup(gl_validationPanel
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								gl_validationPanel
										.createSequentialGroup()
										.addGroup(
												gl_validationPanel
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_validationPanel
																		.createSequentialGroup()
																		.addGap(40)
																		.addGroup(
																				gl_validationPanel
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								lbVerificationTabTitle,
																								GroupLayout.PREFERRED_SIZE,
																								511,
																								GroupLayout.PREFERRED_SIZE)
																						.addGroup(
																								gl_validationPanel
																										.createSequentialGroup()
																										.addGroup(
																												gl_validationPanel
																														.createParallelGroup(
																																Alignment.TRAILING,
																																false)
																														.addComponent(
																																btnCheckStateMachine,
																																Alignment.LEADING,
																																GroupLayout.DEFAULT_SIZE,
																																GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)
																														.addComponent(
																																btn_OpenSpecificationFile,
																																Alignment.LEADING,
																																GroupLayout.DEFAULT_SIZE,
																																152,
																																Short.MAX_VALUE))
																										.addGap(18)
																										.addComponent(
																												tf_SpecificationFilePath4ValidationTab,
																												GroupLayout.PREFERRED_SIZE,
																												391,
																												GroupLayout.PREFERRED_SIZE)))
																		.addGap(80))
														.addGroup(
																gl_validationPanel
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				splitPane_1,
																				GroupLayout.DEFAULT_SIZE,
																				800,
																				Short.MAX_VALUE)))
										.addContainerGap())
						.addGroup(
								gl_validationPanel
										.createSequentialGroup()
										.addContainerGap(636, Short.MAX_VALUE)
										.addComponent(lbLogo1,
												GroupLayout.PREFERRED_SIZE,
												184, GroupLayout.PREFERRED_SIZE))
						.addGroup(
								gl_validationPanel
										.createSequentialGroup()
										.addContainerGap(235, Short.MAX_VALUE)
										.addComponent(lbValidationOutputFile,
												GroupLayout.PREFERRED_SIZE,
												575, GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));
		gl_validationPanel
				.setVerticalGroup(gl_validationPanel
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_validationPanel
										.createSequentialGroup()
										.addGroup(
												gl_validationPanel
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_validationPanel
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				lbVerificationTabTitle,
																				GroupLayout.PREFERRED_SIZE,
																				29,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
																		.addGroup(
																				gl_validationPanel
																						.createParallelGroup(
																								Alignment.BASELINE)
																						.addComponent(
																								btn_OpenSpecificationFile,
																								GroupLayout.PREFERRED_SIZE,
																								33,
																								GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								tf_SpecificationFilePath4ValidationTab,
																								GroupLayout.PREFERRED_SIZE,
																								30,
																								GroupLayout.PREFERRED_SIZE))
																		.addGap(18)
																		.addComponent(
																				btnCheckStateMachine,
																				GroupLayout.PREFERRED_SIZE,
																				32,
																				GroupLayout.PREFERRED_SIZE))
														.addComponent(
																lbLogo1,
																GroupLayout.PREFERRED_SIZE,
																153,
																GroupLayout.PREFERRED_SIZE))
										.addGap(9)
										.addComponent(lbValidationOutputFile,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(splitPane_1,
												GroupLayout.DEFAULT_SIZE, 285,
												Short.MAX_VALUE)
										.addContainerGap()));
		validationPanel.setLayout(gl_validationPanel);

		JPanel verificationPanel = new JPanel();
		verificationPanel.setToolTipText("Verification");
		tabbedPane.addTab("Verification", null, verificationPanel, null);

		JLabel lblprotocolCompletnessChecker = new JLabel(
				"<html><span style=\"color:blue; font-size:17px\">PROTOCOL COMPLETENESS CHECKER</span></html>");

		JButton btn_OpenSpecificationFile4VerificationTab = new JButton(
				"Specification");
		btn_OpenSpecificationFile4VerificationTab
				.setToolTipText("Click to choose specification file");
		btn_OpenSpecificationFile4VerificationTab
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// doOpenSpecificationFile_VerificationTab();
						doOpenSpecificationFile();
					}
				});

		tf_SpecificationFilePath4VerificationTab = new JTextField();
		tf_SpecificationFilePath4VerificationTab.setEditable(false);
		tf_SpecificationFilePath4VerificationTab.setColumns(10);

		btnCheckProtocol = new JButton("Check Protocol");
		btnCheckProtocol.setEnabled(false);
		btnCheckProtocol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCheckProtocol(tf_SpecificationFilePath4VerificationTab
						.getText().trim());
			}
		});
		btnCheckProtocol.setToolTipText("Click to check completness");

		lbVerificationOutputFile = new JLabel("");
		lbVerificationOutputFile
				.setHorizontalTextPosition(SwingConstants.RIGHT);
		lbVerificationOutputFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lbVerificationOutputFile.setForeground(Color.BLUE);

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.5);

		JScrollPane scrollPane = new JScrollPane();
		splitPane_2.setLeftComponent(scrollPane);

		editorPaneSpec4VerificationTab = new JEditorPane();
		editorPaneSpec4VerificationTab.setEditable(false);
		scrollPane.setViewportView(editorPaneSpec4VerificationTab);

		JScrollPane scrollPane_6 = new JScrollPane();
		splitPane_2.setRightComponent(scrollPane_6);

		editorPane_VerificationReport = new JEditorPane();
		editorPane_VerificationReport.setEditable(false);
		scrollPane_6.setViewportView(editorPane_VerificationReport);

		JLabel lbLogo2 = new JLabel("");
		lbLogo2.setIcon(new ImageIcon(BrokerAtCloudVTTS.class
				.getResource(iconPath)));
		GroupLayout gl_verificationPanel = new GroupLayout(verificationPanel);
		gl_verificationPanel.setHorizontalGroup(
			gl_verificationPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_verificationPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_verificationPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_verificationPanel.createSequentialGroup()
							.addGroup(gl_verificationPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_verificationPanel.createSequentialGroup()
									.addGroup(gl_verificationPanel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(btnCheckProtocol, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
										.addComponent(btn_OpenSpecificationFile4VerificationTab, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(tf_SpecificationFilePath4VerificationTab, GroupLayout.PREFERRED_SIZE, 431, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblprotocolCompletnessChecker, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
							.addComponent(lbLogo2, GroupLayout.PREFERRED_SIZE, 184, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_verificationPanel.createSequentialGroup()
							.addGroup(gl_verificationPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(lbVerificationOutputFile, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
								.addComponent(splitPane_2, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE))
							.addContainerGap())))
		);
		gl_verificationPanel.setVerticalGroup(
			gl_verificationPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_verificationPanel.createSequentialGroup()
					.addGroup(gl_verificationPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_verificationPanel.createSequentialGroup()
							.addComponent(lbLogo2, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
							.addGap(9))
						.addGroup(gl_verificationPanel.createSequentialGroup()
							.addComponent(lblprotocolCompletnessChecker, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addGroup(gl_verificationPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(tf_SpecificationFilePath4VerificationTab, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
								.addComponent(btn_OpenSpecificationFile4VerificationTab, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addComponent(btnCheckProtocol, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
							.addGap(18)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbVerificationOutputFile)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(splitPane_2, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
					.addContainerGap())
		);
		verificationPanel.setLayout(gl_verificationPanel);

		testGenerationPanel = new JPanel();
		testGenerationPanel.setToolTipText("Test Generation");
		tabbedPane.addTab("Test Generation", null, testGenerationPanel, null);

		JLabel lblprotocolCompletenessChecker = new JLabel(
				"<html><span style=\"color:blue; font-size:17px\">PROTOCOL COMPLETENESS CHECKER</span></html>");

		JButton button_getInputFile4TestGeneration = new JButton(
				"Specification");
		button_getInputFile4TestGeneration
				.setToolTipText("Click to choose specification file");
		button_getInputFile4TestGeneration
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// doOpenSpecificationFile_TestGenerationTab();
						doOpenSpecificationFile();
					}
				});

		tf_SpecificationFilePath4TestGenerationTab = new JTextField();
		tf_SpecificationFilePath4TestGenerationTab.setEditable(false);
		tf_SpecificationFilePath4TestGenerationTab.setColumns(10);

		btnGenerateTests = new JButton("Generate Tests");

		btnGenerateTests.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label_waiting4TestGeneration.setVisible(true);
				lbPleaseWait4TestGeneration.setVisible(true);
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {

							String path = tf_SpecificationFilePath4TestGenerationTab
									.getText().trim();
							Integer test_depth = (Integer) spinner4TestDepth
									.getValue();
							Boolean multiobjectiveTestFlag = chckbxMultiobjectiveTests
									.isSelected();
							doGenerateTests(path, test_depth,
									multiobjectiveTestFlag);

							label_waiting4TestGeneration.setVisible(false);
							lbPleaseWait4TestGeneration.setVisible(false);

						} catch (Exception ex) {
							//System.out.println("Exception:  " + ex.getMessage());
							JOptionPane.showMessageDialog(null, "An exception occurred: "
									+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							ex.printStackTrace();
						}
					}
				});
				// start the thread
				thread.start();
			}
		});
		btnGenerateTests.setToolTipText("Click to generate test suite");
		btnGenerateTests.setEnabled(false);

		chckbxMultiobjectiveTests = new JCheckBox("Multiobjective tests");
		chckbxMultiobjectiveTests.setSelected(true);

		spinner4TestDepth = new JSpinner();
		spinner4TestDepth.setModel(new SpinnerNumberModel(0, 0, 5, 1));

		JLabel lblNewLabel = new JLabel("Test Depth");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.5);

		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane_3.setLeftComponent(scrollPane_1);

		editorPaneSpec4TestGenerationTab = new JEditorPane();
		editorPaneSpec4TestGenerationTab.setEditable(false);
		scrollPane_1.setViewportView(editorPaneSpec4TestGenerationTab);

		JScrollPane scrollPane_7 = new JScrollPane();
		splitPane_3.setRightComponent(scrollPane_7);

		editorPane_TestGenerationOutputFile = new JEditorPane();
		editorPane_TestGenerationOutputFile.setEditable(false);
		scrollPane_7
				.setViewportView(editorPane_TestGenerationOutputFile);

		JLabel lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setIcon(new ImageIcon(BrokerAtCloudVTTS.class
				.getResource(iconPath)));

		lbAbstractTestsFile = new JLabel("");
		lbAbstractTestsFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lbAbstractTestsFile.setHorizontalTextPosition(SwingConstants.RIGHT);
		lbAbstractTestsFile.setForeground(Color.BLUE);
		
		chckbxPrettyFormattingTests = new JCheckBox("Pretty format (slower)");
		chckbxPrettyFormattingTests.setForeground(Color.BLUE);
		
		// TestGeneration TestGrounding Tab
		label_waiting4TestGrounding = new JLabel("");
		lbPleaseWait4TestGrounding = new JLabel("Please wait...");
		label_waiting4TestGrounding.setIcon(new ImageIcon(BrokerAtCloudVTTS.class
				.getResource(iconWaitingPath)));
		label_waiting4TestGrounding.setVisible(false);
		lbPleaseWait4TestGrounding.setVisible(false);
		
		// TestGeneration TestGeteration Tab
		label_waiting4TestGeneration = new JLabel("");
		lbPleaseWait4TestGeneration = new JLabel("Please wait...");
		label_waiting4TestGeneration.setIcon(new ImageIcon(BrokerAtCloudVTTS.class
				.getResource(iconWaitingPath)));
		label_waiting4TestGeneration.setVisible(false);
		lbPleaseWait4TestGeneration.setVisible(false);
		
		
		panel_1 = new JPanel();
		
		GroupLayout gl_testGenerationPanel = new GroupLayout(
				testGenerationPanel);
		gl_testGenerationPanel.setHorizontalGroup(
			gl_testGenerationPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_testGenerationPanel.createSequentialGroup()
					.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_testGenerationPanel.createSequentialGroup()
							.addGap(28)
							.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_testGenerationPanel.createSequentialGroup()
									.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(lblprotocolCompletenessChecker, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE)
										.addGroup(gl_testGenerationPanel.createSequentialGroup()
											.addComponent(button_getInputFile4TestGeneration, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(tf_SpecificationFilePath4TestGenerationTab, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_testGenerationPanel.createSequentialGroup()
											.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(spinner4TestDepth, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
											.addGap(47)
											.addComponent(chckbxMultiobjectiveTests)))
									.addPreferredGap(ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
									.addComponent(lblNewLabel_2, GroupLayout.PREFERRED_SIZE, 184, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_testGenerationPanel.createSequentialGroup()
									.addComponent(btnGenerateTests, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
									.addGap(53)
									.addComponent(chckbxPrettyFormattingTests)
									.addPreferredGap(ComponentPlacement.RELATED, 228, Short.MAX_VALUE)
									.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)))
							.addGap(0))
						.addGroup(gl_testGenerationPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lbAbstractTestsFile, GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE))
						.addGroup(gl_testGenerationPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(splitPane_3, GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_testGenerationPanel.setVerticalGroup(
			gl_testGenerationPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_testGenerationPanel.createSequentialGroup()
					.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel_2, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_testGenerationPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblprotocolCompletenessChecker, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(button_getInputFile4TestGeneration, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
								.addComponent(tf_SpecificationFilePath4TestGenerationTab, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel)
								.addComponent(spinner4TestDepth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(chckbxMultiobjectiveTests))
							.addGap(18)
							.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_testGenerationPanel.createParallelGroup(Alignment.BASELINE)
									.addComponent(btnGenerateTests, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
									.addComponent(chckbxPrettyFormattingTests)))))
					.addGap(32)
					.addComponent(lbAbstractTestsFile)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(splitPane_3, GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))
		);
		

		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
						.addComponent(label_waiting4TestGeneration, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 189, Short.MAX_VALUE)
						.addComponent(lbPleaseWait4TestGeneration, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
					.addGap(10))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(label_waiting4TestGeneration, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbPleaseWait4TestGeneration, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);
		testGenerationPanel.setLayout(gl_testGenerationPanel);

		JPanel testGroundingPanel = new JPanel();
		testGroundingPanel.setToolTipText("Test Grounding");
		tabbedPane.addTab("Test Grounding", null, testGroundingPanel, null);

		JLabel lblplatformSpecificJunit = new JLabel(
				"<html><span style=\"color:blue; font-size:17px\">PLATFORM SPECIFIC JUNIT TEST GROUNDING</span></html>");

		JButton btnTestFile = new JButton("Test File");
		btnTestFile.setToolTipText("Click to choose abstract tests file");
		btnTestFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpenTestFile_TestGroundingTab();
			}
		});

		textField_TestFilePath_GroundingTab = new JTextField();
		textField_TestFilePath_GroundingTab.setEditable(false);
		textField_TestFilePath_GroundingTab.setColumns(10);

		btnGenerateJunitTests = new JButton("Generate JUnit Tests");
		btnGenerateJunitTests.setActionCommand("Generate JUnit Tests");
		btnGenerateJunitTests.setEnabled(false);
		btnGenerateJunitTests.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	        	label_waiting4TestGrounding.setVisible(true);
	        	lbPleaseWait4TestGrounding.setVisible(true);
	        	
		           Thread thread = new Thread(new Runnable() {
		                public void run() {
		                    try {     	
		  	                    	String path = textField_TestFilePath_GroundingTab
		  									.getText().trim();
		  							String groundingParam = (String) comboBox_grounding
		  									.getSelectedItem();
		  							Boolean metaCheckParam = chckbxVerifyTestsTransitions
		  									.isSelected();
		  							String remoteServiceBaseURLParam = textField_RemoteServiceBase
		  									.getText().trim();
		  							String targetPackageParam = textField_TestDriver
		  									.getText().trim();
		  							String sourcePackagesParam = tf_ServiceClientSourcePackages
		  									.getText().trim();
		  						
		  							doTestGrounding(path, groundingParam,
		  									metaCheckParam, remoteServiceBaseURLParam,
		  									targetPackageParam, sourcePackagesParam);

		  							label_waiting4TestGrounding.setVisible(false);	
		  							lbPleaseWait4TestGrounding.setVisible(false);

		                    } catch (Exception ex) {
		                        // System.out.println("Exception:  " + ex.getMessage());
		    					JOptionPane.showMessageDialog(null, "An exception occurred: "
		    							+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		    					ex.printStackTrace();
		                    }
		                }
		            });
		        // start the thread
		        thread.start();
		        // System.out.println("End generateResultsOnSeparatedThread EVENT");
			}
		});
		btnGenerateJunitTests.setToolTipText("Click to generate grounded tests");

		chckbxVerifyTestsTransitions = new JCheckBox("Verify states/transitions");
		chckbxVerifyTestsTransitions.setSelected(true);

		comboBox_grounding = new JComboBox<String>();
		comboBox_grounding.setModel(new DefaultComboBoxModel<String>(
				new String[] { "Java", "JAX-RS", "JAX-WS" }));

		JLabel lblNewLabel_1 = new JLabel("Grounding");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);

		JLabel lblRemoteServiceBase = new JLabel("Remote service base URL");
		lblRemoteServiceBase.setHorizontalAlignment(SwingConstants.RIGHT);

		textField_RemoteServiceBase = new JTextField();
		textField_RemoteServiceBase.setColumns(10);

		textField_TestDriver = new JTextField();
		textField_TestDriver.setColumns(10);

		JLabel lblTestDriver = new JLabel("Test driver target package");
		lblTestDriver.setHorizontalAlignment(SwingConstants.RIGHT);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);

		JLabel lblServiceClientSource = new JLabel(
				"Service client source package(s)");
		lblServiceClientSource.setHorizontalAlignment(SwingConstants.RIGHT);

		tf_ServiceClientSourcePackages = new JTextField();
		tf_ServiceClientSourcePackages.setColumns(10);

		JLabel label_1 = new JLabel("");
		label_1.setIcon(new ImageIcon(BrokerAtCloudVTTS.class
				.getResource(iconPath)));

		lbJUnitFile = new JLabel("");
		lbJUnitFile.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lbJUnitFile.setHorizontalTextPosition(SwingConstants.RIGHT);
		lbJUnitFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lbJUnitFile.setForeground(Color.BLUE);
		
		chckbxPrettyFormattingJUnit = new JCheckBox("Pretty format");
		chckbxPrettyFormattingJUnit.setForeground(Color.BLUE);
		
		JPanel panel = new JPanel();
		GroupLayout gl_testGroundingPanel = new GroupLayout(testGroundingPanel);
		gl_testGroundingPanel.setHorizontalGroup(
			gl_testGroundingPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_testGroundingPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_testGroundingPanel.createSequentialGroup()
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_testGroundingPanel.createSequentialGroup()
									.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_testGroundingPanel.createSequentialGroup()
											.addComponent(lblplatformSpecificJunit, GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
											.addGap(69))
										.addGroup(gl_testGroundingPanel.createSequentialGroup()
											.addGap(4)
											.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_testGroundingPanel.createSequentialGroup()
													.addComponent(btnTestFile, GroupLayout.PREFERRED_SIZE, 157, GroupLayout.PREFERRED_SIZE)
													.addGap(15)
													.addComponent(textField_TestFilePath_GroundingTab, GroupLayout.PREFERRED_SIZE, 393, GroupLayout.PREFERRED_SIZE))
												.addGroup(gl_testGroundingPanel.createSequentialGroup()
													.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(ComponentPlacement.UNRELATED)
													.addComponent(comboBox_grounding, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(ComponentPlacement.UNRELATED)
													.addComponent(chckbxVerifyTestsTransitions)
													.addPreferredGap(ComponentPlacement.UNRELATED)
													.addComponent(chckbxPrettyFormattingJUnit, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(ComponentPlacement.RELATED)
													.addComponent(btnGenerateJunitTests, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)))
											.addGap(8)))
									.addGap(29))
								.addGroup(gl_testGroundingPanel.createSequentialGroup()
									.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_testGroundingPanel.createSequentialGroup()
											.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING, false)
												.addComponent(lblServiceClientSource, GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
												.addComponent(lblTestDriver, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
											.addGap(15))
										.addGroup(gl_testGroundingPanel.createSequentialGroup()
											.addComponent(lblRemoteServiceBase)
											.addGap(18)))
									.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING, false)
										.addComponent(tf_ServiceClientSourcePackages)
										.addComponent(textField_RemoteServiceBase)
										.addComponent(textField_TestDriver, GroupLayout.PREFERRED_SIZE, 411, GroupLayout.PREFERRED_SIZE))))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(label_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panel, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)))
						.addComponent(splitPane)
						.addComponent(lbJUnitFile, GroupLayout.PREFERRED_SIZE, 620, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_testGroundingPanel.setVerticalGroup(
			gl_testGroundingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_testGroundingPanel.createSequentialGroup()
					.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_testGroundingPanel.createSequentialGroup()
							.addGap(32)
							.addComponent(lblplatformSpecificJunit, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_testGroundingPanel.createSequentialGroup()
									.addGap(23)
									.addComponent(textField_TestFilePath_GroundingTab, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_testGroundingPanel.createSequentialGroup()
									.addGap(20)
									.addComponent(btnTestFile, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)))
							.addGap(18)
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnGenerateJunitTests, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
								.addComponent(chckbxPrettyFormattingJUnit)
								.addComponent(chckbxVerifyTestsTransitions)
								.addComponent(comboBox_grounding, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_RemoteServiceBase, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblRemoteServiceBase, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblTestDriver, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
								.addComponent(textField_TestDriver, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
							.addGap(9)
							.addGroup(gl_testGroundingPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblServiceClientSource, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
								.addComponent(tf_ServiceClientSourcePackages, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_testGroundingPanel.createSequentialGroup()
							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbJUnitFile)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
		);
		
	   
		lbPleaseWait4TestGrounding.setVerticalAlignment(SwingConstants.TOP);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(label_waiting4TestGrounding, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 155, Short.MAX_VALUE)
						.addComponent(lbPleaseWait4TestGrounding, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(18)
					.addComponent(label_waiting4TestGrounding, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbPleaseWait4TestGrounding, GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
					.addContainerGap())
		);
		panel.setLayout(gl_panel);

		JScrollPane scrollPane_2 = new JScrollPane();
		splitPane.setLeftComponent(scrollPane_2);

		editorPaneGrounding_InputFile = new JEditorPane();
		editorPaneGrounding_InputFile.setEditable(false);
		scrollPane_2.setViewportView(editorPaneGrounding_InputFile);

		JScrollPane scrollPane_3 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_3);

		editorPaneGrounding_JUnitOutputFile = new JEditorPane();
		editorPaneGrounding_JUnitOutputFile.setEditable(false);
		scrollPane_3.setViewportView(editorPaneGrounding_JUnitOutputFile);
		testGroundingPanel.setLayout(gl_testGroundingPanel);
		contentPane.setLayout(gl_contentPane);
	}
}
