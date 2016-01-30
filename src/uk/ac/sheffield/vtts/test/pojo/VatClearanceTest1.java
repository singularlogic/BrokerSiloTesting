package uk.ac.sheffield.vtts.test.pojo;

import uk.ac.sheffield.vtts.client.pojo.Invoice;
import uk.ac.sheffield.vtts.client.pojo.Vat;
import org.junit.*;
import static org.junit.Assert.*;

import uk.ac.sheffield.vtts.ground.JavaFactory;
import uk.ac.sheffield.vtts.client.pojo.*;

/**
 * VatClearanceTest generated on Sat Jan 30 12:58:20 EET 2016
 * by Broker@Cloud generator uk.ac.sheffield.vtts.ground.JavaGrounding
 *
 * System-Under-Test (SUT) is the POJO Java class: VatClearance,
 * assumed to be a 'Plain Old Java Object'.
 *
 * This is a simple demonstration of grounding to Java, as though the
 * tested artifact were a simple Java class (POJO) offering methods that
 * might correspond to the operations of a service.  We assume that the
 * POJO can be reset simply by creating a fresh instance.  The names of
 * the POJO's methods must correspond to the original names used in the
 * specification.  If the POJO offers full inspection of its internal
 * behaviour, it must offer two further methods, <em>getScenario()</em>
 * and <em>getState()</em> to report on its status.
 *
 *		Exploring all paths up to length: 1
 *		Number of theoretical sequences: 46
 *		Number of infeasible sequences: 14
 *		Number of redundant sequences: 0
 *		Number of executable sequences: 32
 *		Warning: specification is not fully covered!
 */
public class VatClearanceTest1 {

	/**
	 * The Java object representing the System-Under-Test.
	 */
	private VatClearance system = null;

	/**
	 * JavaFactory to synthesise objects of complex types.
	 */
	private JavaFactory factory;

	/**
	 * Creates the JUnit test driver: VatClearanceTest.
	 */
	public VatClearanceTest1() {
		factory = new JavaFactory();
		factory.useSourcePackage("uk.ac.sheffield.vtts.client.pojo");
	}

	/**
	 * Creates a fresh instance of the System-Under-Test before each
	 * test.  Verifies that creation works before the first test.
	 */
	@Before
	public void setUp() {
		if (system == null) {
			system = new VatClearance();
			assertNotNull(system);
		}
		else
			system = new VatClearance();
	}

	/**
	 * Translation of TestSequence #1.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 0.
	 */
	@Test
	public void test1 () {
		assertEquals("create/ok", system.getScenario());
		assertEquals("Initial", system.getState());
	}

	/**
	 * Translation of TestSequence #2.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test2 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		// Verify invocation step #1
		assertEquals("enterVatDeclaration/ok", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #3.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test3 () {
		system.enterVatDeclaration((Vat)
			factory.create("99893577", "Vat"));
		// Verify invocation step #1
		assertEquals("enterVatDeclaration/error", system.getScenario());
		assertEquals("Initial", system.getState());
	}

	/**
	 * Translation of TestSequence #4.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test4 () {
		system.exitVatDeclaration();
		// Verify invocation step #1
		assertEquals("exitVatDeclaration/ok", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #5.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test5 () {
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #1
		assertEquals("addInvoice/ignore", system.getScenario());
		assertEquals("Initial", system.getState());
	}

	/**
	 * Translation of TestSequence #6.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test6 () {
		system.removeInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #1
		assertEquals("removeInvoice/ignore", system.getScenario());
		assertEquals("Initial", system.getState());
	}

	/**
	 * Translation of TestSequence #7.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test7 () {
		system.performClearance();
		// Verify invocation step #1
		assertEquals("performClearance/ignore", system.getScenario());
		assertEquals("Initial", system.getState());
	}

	/**
	 * Translation of TestSequence #8.  The main test goal is to reach the 
	 * state 'Initial' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test8 () {
		Double refund1 = system.payAmount();
		// Verify invocation step #1
		assertEquals((Double) null, refund1);
		assertEquals("payAmount/ignore", system.getScenario());
		assertEquals("Initial", system.getState());
	}

	/**
	 * Translation of TestSequence #9.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test9 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		// Verify invocation step #2
		assertEquals("enterVatDeclaration/ignore", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #10.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test10 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.enterVatDeclaration((Vat)
			factory.create("99893577", "Vat"));
		// Verify invocation step #2
		assertEquals("enterVatDeclaration/ignore", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #11.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test11 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.exitVatDeclaration();
		// Verify invocation step #2
		assertEquals("exitVatDeclaration/ok", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #12.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test12 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #2
		assertEquals("addInvoice/ok", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #13.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test13 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.removeInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #2
		assertEquals("removeInvoice/error", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #14.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test14 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.performClearance();
		// Verify invocation step #2
		assertEquals("performClearance/error", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #15.  The main test goal is to reach the 
	 * state 'VatDeclaration' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test15 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		Double refund2 = system.payAmount();
		// Verify invocation step #2
		assertEquals((Double) null, refund2);
		assertEquals("payAmount/ignore", system.getScenario());
		assertEquals("VatDeclaration", system.getState());
	}

	/**
	 * Translation of TestSequence #16.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test16 () {
		system.exitVatDeclaration();
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		// Verify invocation step #2
		assertEquals("enterVatDeclaration/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #17.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test17 () {
		system.exitVatDeclaration();
		system.enterVatDeclaration((Vat)
			factory.create("99893577", "Vat"));
		// Verify invocation step #2
		assertEquals("enterVatDeclaration/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #18.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test18 () {
		system.exitVatDeclaration();
		system.exitVatDeclaration();
		// Verify invocation step #2
		assertEquals("exitVatDeclaration/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #19.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test19 () {
		system.exitVatDeclaration();
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #2
		assertEquals("addInvoice/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #20.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test20 () {
		system.exitVatDeclaration();
		system.removeInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #2
		assertEquals("removeInvoice/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #21.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test21 () {
		system.exitVatDeclaration();
		system.performClearance();
		// Verify invocation step #2
		assertEquals("performClearance/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #22.  The main test goal is to reach the 
	 * state 'Final' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test22 () {
		system.exitVatDeclaration();
		Double refund2 = system.payAmount();
		// Verify invocation step #2
		assertEquals((Double) null, refund2);
		assertEquals("payAmount/ignore", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #23.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 0.
	 */
	@Test
	public void test23 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		// Verify invocation step #3
		assertEquals("performClearance/ok", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #24.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test24 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		// Verify invocation step #4
		assertEquals("enterVatDeclaration/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #25.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test25 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.enterVatDeclaration((Vat)
			factory.create("99893577", "Vat"));
		// Verify invocation step #4
		assertEquals("enterVatDeclaration/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #26.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test26 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.exitVatDeclaration();
		// Verify invocation step #4
		assertEquals("exitVatDeclaration/ok", system.getScenario());
		assertEquals("Final", system.getState());
	}

	/**
	 * Translation of TestSequence #27.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test27 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.addInvoice((Invoice)
			factory.create("invoice2", "Invoice"));
		// Verify invocation step #4
		assertEquals("addInvoice/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #28.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test28 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #4
		assertEquals("addInvoice/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #29.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test29 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.removeInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		// Verify invocation step #4
		assertEquals("removeInvoice/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #30.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test30 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.removeInvoice((Invoice)
			factory.create("invoice2", "Invoice"));
		// Verify invocation step #4
		assertEquals("removeInvoice/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #31.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test31 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		system.performClearance();
		// Verify invocation step #4
		assertEquals("performClearance/ignore", system.getScenario());
		assertEquals("Clearance", system.getState());
	}

	/**
	 * Translation of TestSequence #32.  The main test goal is to reach the 
	 * state 'Clearance' and, from there, execute a novel path of length 1.
	 */
	@Test
	public void test32 () {
		system.enterVatDeclaration((Vat)
			factory.create("107539588", "Vat"));
		system.addInvoice((Invoice)
			factory.create("invoice1", "Invoice"));
		system.performClearance();
		Double refund4 = system.payAmount();
		// Verify invocation step #4
		assertEquals((Double) 0.0, refund4);
		assertEquals("payAmount/debit", system.getScenario());
		assertEquals("Final", system.getState());
	}
}

