package com.lendwise.soa.client;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * JAX-WS SOAP client that calls LoanDBProcess SOA composite via WSDL.
 * This is the entry point that triggers the BPEL orchestration which
 * invokes multiple Database Adapters for loan processing.
 *
 * WSDL Contract: loan-db-composite/LoanDBProcess.wsdl
 * Service: loandb_client_ep
 * PortType: LoanDBProcess
 * Operations: submitLoanApplication, getLoanStatus, updateLoanDecision
 * Endpoint: http://localhost:8001/soap/LoanDBProcess
 */
public class LoanDBClient {

    private static final String WSDL_URL = "http://localhost:8001/soap/LoanDBProcess?wsdl";
    private static final String NAMESPACE = "http://xmlns.oracle.com/LendWise/LoanDBProcess";
    private static final String SERVICE_NAME = "loandb_client_ep";

    private final LoanDBProcessPort port;

    public LoanDBClient() throws Exception {
        URL wsdlURL = new URL(WSDL_URL);
        QName serviceName = new QName(NAMESPACE, SERVICE_NAME);
        Service service = Service.create(wsdlURL, serviceName);
        this.port = service.getPort(LoanDBProcessPort.class);
    }

    public LoanDBClient(String wsdlUrl) throws Exception {
        URL wsdlURL = new URL(wsdlUrl);
        QName serviceName = new QName(NAMESPACE, SERVICE_NAME);
        Service service = Service.create(wsdlURL, serviceName);
        this.port = service.getPort(LoanDBProcessPort.class);
    }

    /**
     * Submits a loan application via SOA composite.
     * This triggers BPEL orchestration which:
     * 1. Inserts borrower record (BorrowerDB.insertBorrower)
     * 2. Inserts loan application (LoanDB.insertLoanApplication)
     * 3. Calculates LTV ratio (LoanDB.calculateLTV - stored procedure)
     * 4. Runs risk assessment (DecisionDB.runRiskAssessment - stored procedure)
     * 5. Inserts decision record (DecisionDB.insertDecision)
     */
    public LoanApplicationResponse submitLoanApplication(LoanApplicationRequest request) {
        System.out.println("[LoanDBClient] Calling SOA Suite: submitLoanApplication");
        System.out.println("[LoanDBClient] WSDL: " + WSDL_URL);
        System.out.println("[LoanDBClient] Applicant SSN: " + request.getSsn());
        System.out.println("[LoanDBClient] Loan Amount: $" + request.getLoanAmount());
        System.out.println("[LoanDBClient] Property Value: $" + request.getPropertyValue());

        LoanApplicationResponse response = port.submitLoanApplication(request);

        System.out.println("[LoanDBClient] Response received from SOA Suite");
        System.out.println("[LoanDBClient] Loan ID: " + response.getLoanId());
        System.out.println("[LoanDBClient] Decision: " + response.getDecision());
        return response;
    }

    /**
     * Gets loan status by ID.
     * Triggers BPEL orchestration which:
     * 1. Selects loan by ID (LoanDB.selectLoanById)
     * 2. Selects decision by loan (DecisionDB.selectDecisionByLoan)
     */
    public LoanStatusResponse getLoanStatus(String loanId) {
        System.out.println("[LoanDBClient] Calling SOA Suite: getLoanStatus");
        System.out.println("[LoanDBClient] Loan ID: " + loanId);

        LoanStatusResponse response = port.getLoanStatus(loanId);

        System.out.println("[LoanDBClient] Status: " + response.getStatus());
        return response;
    }

    /**
     * Updates loan decision (for manual review cases).
     * Triggers BPEL orchestration which:
     * 1. Updates loan application (LoanDB.updateLoanApplication)
     * 2. Inserts new decision (DecisionDB.insertDecision)
     */
    public UpdateDecisionResponse updateLoanDecision(UpdateDecisionRequest request) {
        System.out.println("[LoanDBClient] Calling SOA Suite: updateLoanDecision");
        System.out.println("[LoanDBClient] Loan ID: " + request.getLoanId());
        System.out.println("[LoanDBClient] New Decision: " + request.getDecision());

        UpdateDecisionResponse response = port.updateLoanDecision(request);

        System.out.println("[LoanDBClient] Update Status: " + response.getStatus());
        return response;
    }

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("     LOAN DB CLIENT - Calling SOA Suite Database Adapters via WSDL");
        System.out.println("=========================================================================");
        System.out.println("Target WSDL: " + WSDL_URL);
        System.out.println("Target Namespace: " + NAMESPACE);
        System.out.println("Service Name: " + SERVICE_NAME);
        System.out.println("=========================================================================");

        try {
            LoanDBClient client = new LoanDBClient();

            // Test Case 1: Submit new loan application
            System.out.println("\n--- TEST CASE 1: Submit Loan Application ---");
            LoanApplicationRequest request1 = new LoanApplicationRequest();
            request1.setApplicantName("John Doe");
            request1.setSsn("123-45-6789");
            request1.setLoanAmount(250000.0);
            request1.setPropertyValue(400000.0);
            request1.setMonthlyIncome(12000.0);
            request1.setPropertyAddress("123 Main St, VA 22030");

            LoanApplicationResponse response1 = client.submitLoanApplication(request1);
            printApplicationResponse(response1);

            // Test Case 2: Get loan status
            System.out.println("\n--- TEST CASE 2: Get Loan Status ---");
            LoanStatusResponse response2 = client.getLoanStatus(response1.getLoanId());
            printStatusResponse(response2);

            // Test Case 3: Update decision (manual review)
            System.out.println("\n--- TEST CASE 3: Update Loan Decision ---");
            UpdateDecisionRequest request3 = new UpdateDecisionRequest();
            request3.setLoanId(response1.getLoanId());
            request3.setDecision("APPROVED");
            request3.setReviewerNotes("Manual review completed - approved with conditions");

            UpdateDecisionResponse response3 = client.updateLoanDecision(request3);
            printUpdateResponse(response3);

        } catch (Exception e) {
            System.err.println("[LoanDBClient] Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=========================================================================");
    }

    private static void printApplicationResponse(LoanApplicationResponse response) {
        System.out.println("[RESPONSE] Loan ID: " + response.getLoanId());
        System.out.println("[RESPONSE] Decision: " + response.getDecision());
        System.out.println("[RESPONSE] LTV Ratio: " + response.getLtvRatio() + "%");
        System.out.println("[RESPONSE] Risk Score: " + response.getRiskScore());
        System.out.println("[RESPONSE] Notes: " + response.getDecisionNotes());
    }

    private static void printStatusResponse(LoanStatusResponse response) {
        System.out.println("[RESPONSE] Status: " + response.getStatus());
        System.out.println("[RESPONSE] Decision: " + response.getDecision());
        System.out.println("[RESPONSE] Last Updated: " + response.getLastUpdated());
    }

    private static void printUpdateResponse(UpdateDecisionResponse response) {
        System.out.println("[RESPONSE] Status: " + response.getStatus());
        System.out.println("[RESPONSE] Updated At: " + response.getUpdatedAt());
    }
}

// Supporting classes (normally generated from WSDL)
interface LoanDBProcessPort {
    LoanApplicationResponse submitLoanApplication(LoanApplicationRequest request);
    LoanStatusResponse getLoanStatus(String loanId);
    UpdateDecisionResponse updateLoanDecision(UpdateDecisionRequest request);
}

class LoanApplicationRequest {
    private String applicantName;
    private String ssn;
    private double loanAmount;
    private double propertyValue;
    private double monthlyIncome;
    private String propertyAddress;

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(double loanAmount) { this.loanAmount = loanAmount; }
    public double getPropertyValue() { return propertyValue; }
    public void setPropertyValue(double propertyValue) { this.propertyValue = propertyValue; }
    public double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
}

class LoanApplicationResponse {
    private String loanId;
    private String decision;
    private double ltvRatio;
    private int riskScore;
    private String decisionNotes;

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public double getLtvRatio() { return ltvRatio; }
    public void setLtvRatio(double ltvRatio) { this.ltvRatio = ltvRatio; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }
}

class LoanStatusResponse {
    private String status;
    private String decision;
    private String lastUpdated;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}

class UpdateDecisionRequest {
    private String loanId;
    private String decision;
    private String reviewerNotes;

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReviewerNotes() { return reviewerNotes; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }
}

class UpdateDecisionResponse {
    private String status;
    private String updatedAt;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
