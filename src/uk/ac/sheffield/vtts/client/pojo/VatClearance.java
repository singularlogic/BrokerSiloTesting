package uk.ac.sheffield.vtts.client.pojo;

import uk.ac.sheffield.vtts.client.pojo.Invoice;
import uk.ac.sheffield.vtts.client.pojo.Vat;

/**
 *
 * @author Panagiotis Gouvas
 */
public class VatClearance {

    /*
     * State space: Initial, VatDeclaration, Clearance, Final
     * Scenario space: "enterVatDeclaration/ok" "enterVatDeclaration/error" "exitVatDeclaration/ok" "addInvoice/ok" "addInvoice/error" 
     * "removeInvoice/ok" "removeInvoice/error" "performClearance/ok" "performClearance/error" "exitVatDeclaration/ok"
     * "payAmount/debit"  "payAmount/credit"
     * "Final"
     */
    
    private String state="Initial";
    private String scenario="create/ok";

    public VatClearance() {

    }

    public Object getScenario() {
        return this.scenario;
    }

    public Object getState() {
        return this.state;
    }

    public void enterVatDeclaration(Vat vat) {
        this.state="VatDeclaration";
    }

    public void exitVatDeclaration() {
        this.state="Final";
    }

    public void addInvoice(Invoice invoice) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void removeInvoice(Invoice invoice) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void performClearance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Double payAmount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}//EoC VatClearance

