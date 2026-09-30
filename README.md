# SOA Database Adapter Pattern - Test Project

This project demonstrates the **Oracle SOA Suite Database Adapter** call chain pattern for CodeScout parser testing.

## Call Chain Architecture

```
┌─────────────────┐     ┌───────────────────┐     ┌──────────────────┐
│   Java Client   │────>│      WSDL         │────>│   composite.xml  │
│ (SOAP Request)  │     │ LoanDBProcess.wsdl│     │   SOA_SERVICE    │
└─────────────────┘     └───────────────────┘     └──────────────────┘
                                                          │
                                                          ▼
                                                  ┌──────────────────┐
                                                  │  SOA_COMPONENT   │
                                                  │ (LoanDBProcess)  │
                                                  └──────────────────┘
                                                          │
                                                          ▼
                                                  ┌──────────────────┐
                                                  │    BPEL_ORCH     │
                                                  │ LoanDBProcess.bpel│
                                                  └──────────────────┘
                                                          │
                         ┌────────────────────────────────┼────────────────────────────────┐
                         ▼                                ▼                                ▼
                 ┌───────────────┐               ┌───────────────┐               ┌───────────────┐
                 │ SOA_REFERENCE │               │ SOA_REFERENCE │               │ SOA_REFERENCE │
                 │ LoanDB        │               │ BorrowerDB    │               │ DecisionDB    │
                 └───────────────┘               └───────────────┘               └───────────────┘
                         │                                │                                │
                         ▼                                ▼                                ▼
                 ┌───────────────┐               ┌───────────────┐               ┌───────────────┐
                 │JCA_ADAPTER_DB │               │JCA_ADAPTER_DB │               │JCA_ADAPTER_DB │
                 │  LoanDB.jca   │               │ BorrowerDB.jca│               │ DecisionDB.jca│
                 └───────────────┘               └───────────────┘               └───────────────┘
                         │                                │                                │
                         ▼                                ▼                                ▼
                 ┌───────────────┐               ┌───────────────┐               ┌───────────────┐
                 │DatabaseEntity │               │DatabaseEntity │               │DatabaseEntity │
                 │ LOAN_APPS     │               │ BORROWERS     │               │ DECISIONS     │
                 └───────────────┘               └───────────────┘               └───────────────┘
```

## Project Structure

```
SOA-DatabaseAdapter-Pattern/
├── README.md
├── java-client/
│   └── src/com/lendwise/soa/client/
│       └── LoanDBClient.java           # SOAP client entry point
├── loan-db-composite/
│   ├── composite.xml                    # Main orchestration wiring
│   ├── LoanDBProcess.wsdl              # Service contract
│   ├── LoanDBTypes.xsd                 # Data types
│   ├── LoanDBProcess.bpel              # BPEL orchestration
│   └── adapters/
│       ├── LoanDB.jca                  # Loan Application DB adapter
│       ├── LoanDB.wsdl                 # Loan DB adapter WSDL
│       ├── BorrowerDB.jca              # Borrower DB adapter
│       ├── BorrowerDB.wsdl             # Borrower DB adapter WSDL
│       ├── DecisionDB.jca              # Decision DB adapter
│       └── DecisionDB.wsdl             # Decision DB adapter WSDL
└── database/
    └── schema/
        └── create_loan_tables.sql       # Database schema
```

## Database Adapter Operations

### LoanDB.jca
| Operation | Interaction Spec | DmlType | DescriptorName |
|-----------|-----------------|---------|----------------|
| insertLoanApplication | DBWriteInteractionSpec | INSERT | LoanDB.LoanApplication |
| updateLoanApplication | DBWriteInteractionSpec | UPDATE | LoanDB.LoanApplication |
| selectLoanById | DBReadInteractionSpec | SELECT | LoanDB.LoanApplication |
| calculateLTV | DBStoredProcedureInteractionSpec | STORED_PROC | LOAN_PKG.CALCULATE_LTV |

### BorrowerDB.jca
| Operation | Interaction Spec | DmlType | DescriptorName |
|-----------|-----------------|---------|----------------|
| insertBorrower | DBWriteInteractionSpec | INSERT | BorrowerDB.Borrower |
| selectBorrowerBySSN | DBReadInteractionSpec | SELECT | BorrowerDB.Borrower |
| updateBorrowerCredit | DBWriteInteractionSpec | UPDATE | BorrowerDB.Borrower |

### DecisionDB.jca
| Operation | Interaction Spec | DmlType | DescriptorName |
|-----------|-----------------|---------|----------------|
| insertDecision | DBWriteInteractionSpec | INSERT | DecisionDB.LoanDecision |
| selectDecisionByLoan | DBReadInteractionSpec | SELECT | DecisionDB.LoanDecision |
| runRiskAssessment | DBStoredProcedureInteractionSpec | STORED_PROC | DECISION_PKG.RUN_RISK_ASSESSMENT |

## Expected Parser Output

### Snippet Types
- `FUNCTION` - Java client methods
- `WSDL_OPERATION` - WSDL operation definitions
- `SOA_SERVICE` - Composite service entry points
- `SOA_COMPONENT` - BPEL component bindings
- `BPEL_ORCH` - BPEL process orchestration
- `SOA_REFERENCE` - Outbound reference bindings
- `JCA_ADAPTER_DB` - Database adapter configurations
- `DatabaseEntity` - Database table/entity nodes

### Callee Types
- `JAVA_SOAP_CALL` - Java → WSDL
- `WSDL_TO_SERVICE_ENTRY` - WSDL → SOA_SERVICE
- `SERVICE_TO_COMPONENT` - SOA_SERVICE → SOA_COMPONENT
- `COMPONENT_TO_BPEL` - SOA_COMPONENT → BPEL_ORCH
- `BPEL_TO_REFERENCE` - BPEL → SOA_REFERENCE
- `REFERENCE_TO_JCA_DB` - SOA_REFERENCE → JCA_ADAPTER_DB
- `JCA_DB_TO_ENTITY` - JCA_ADAPTER_DB → DatabaseEntity

## Testing

Run parser on this project (ID: TBD):
```bash
dotnet run --project ParserService.Runner -- "<path>/SOA-DatabaseAdapter-Pattern" java
```
