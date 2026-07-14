package com.bank.console;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Thin wrapper around Scanner that centralizes prompting and input parsing
 * so the main application loop stays focused on orchestration, not I/O details.
 */
public class ConsoleMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void printHeader() {
        System.out.println("""

                ==========================================================
                       SIMPLE BANKING TRANSACTION MANAGEMENT SYSTEM
                ==========================================================
                """);
    }

    public int printMainMenuAndReadChoice() {
        System.out.println("""
                --------------------------- MAIN MENU ---------------------------
                 1.  Add Customer
                 2.  View Customers
                 3.  Open Account
                 4.  View Account
                 5.  Deposit Money
                 6.  Withdraw Money
                 7.  Transfer Money
                 8.  View Transaction History
                 9.  Search and Filter Transactions
                10.  Generate Banking Reports
                11.  Close Account
                12.  Process Bulk Transactions (Multi-threaded Demo)
                13.  Save Data
                 0.  Exit
                -------------------------------------------------------------------""");
        return readInt("Enter your choice: ");
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    public BigDecimal readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount (e.g. 100.50).");
            }
        }
    }

    public void printLine(String message) {
        System.out.println(message);
    }

    public void printError(String message) {
        System.out.println("ERROR: " + message);
    }

    public void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
