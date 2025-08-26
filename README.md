# OOP 2025 – Semester Project

This project is an implementation of the **B-OOP 2025** semester assignment.  
The goal is to design a simplified **insurance system** for small insurance companies in **Java**.

## Project Overview

The system supports three types of insurance contracts:

- **SingleVehicleContract** – compulsory liability insurance (PZP) for a single vehicle.  
- **MasterVehicleContract** – framework contract for a fleet of vehicles.  
- **TravelContract** – travel insurance.  

The system is managed by the **InsuranceCompany** class, which handles:
- creating and managing contracts,  
- processing payments via **PaymentHandler**,  
- evaluating contract due dates,  
- handling insurance claims.  

Insured objects include:
- **Person** (natural or legal person),  
- **Vehicle** (car, bus, etc.).  

## Requirements

- **Java 17+**  
- IDE or build tool (e.g., IntelliJ IDEA, Eclipse, or VS Code with Java support)  

## Running the Tests

The project contains a `test/` directory with **RequiredTests.java**, which verifies the correctness of the implementation.

1. Place the `test` directory at the same level as `src`.  
2. Run the `RequiredTests` (JUnit tests).  
3. All tests must pass to meet the assignment requirements.  

## Evaluation

Grading is based on:
1. **Automatic testing**:
   - Verification of archive structure,  
   - UML compliance,  
   - Passing all unit tests,  
   - Hidden private tests,  
   - Anti-plagiarism check.  
2. **Oral defense**:
   - Demonstrating understanding of your solution,  
   - Explaining OOP principles used,  
   - Clean code and adherence to Java conventions.  

---

## Author
Semester project – B-OOP 2025  
Faculty of Electrical Engineering and Information Technology, Slovak University of Technology in Bratislava
