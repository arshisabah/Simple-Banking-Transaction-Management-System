package com.bank.console;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.AccountType;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.TransactionRequest;
import com.bank.model.TransactionRequestResult;
import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.AccountService;
import com.bank.service.BatchTransactionProcessor;
import com.bank.service.CustomerService;
import com.bank.service.ReportService;
import com.bank.service.TransactionSearchService;
import com.bank.service.TransactionService;
import com.bank.util.FileExporter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application entry point and console-menu driver.
 * This class only coordinates user input -> service calls -> printed output;
 * all business rules live in the service layer.
 */
public class BankConsoleApp {

    private final ConsoleMenu menu = new ConsoleMenu();

    private final CustomerRepository customerRepository = new CustomerRepository();
    private final AccountRepository accountRepository = new AccountRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    private final CustomerService customerService = new CustomerService(customerRepository, accountRepository);
    private final AccountService accountService = new AccountService(accountRepository, customerRepository);
    private final TransactionService transactionService = new TransactionService(accountRepository, transactionRepository);
    private final TransactionSearchService searchService = new TransactionSearchService(transactionRepository);
    private final ReportService reportService = new ReportService(customerRepository, accountRepository, transactionRepository);
    private final BatchTransactionProcessor batchProcessor = new BatchTransactionProcessor(transactionService);

    public void run() {
        menu.printHeader();
        boolean running = true;
        while (running) {
            int choice = menu.printMainMenuAndReadChoice();
            try {
                running = handleChoice(choice);
            } catch (BankException e) {
                menu.printError(e.getMessage());
            } catch (Exception e) {
                menu.printError("Unexpected error: " + e.getMessage());
            }
            if (running && choice != 0) {
                menu.pressEnterToContinue();
            }
        }
        menu.printLine("Thank you for using the Banking System. Goodbye!");
        menu.close();
    }

    /** Returns false only when the user chooses to exit. */
    private boolean handleChoice(int choice) throws BankException {
        switch (choice) {
            case 1 -> addCustomer();
            case 2 -> viewCustomers();
            case 3 -> openAccount();
            case 4 -> viewAccount();
            case 5 -> depositMoney();
            case 6 -> withdrawMoney();
            case 7 -> transferMoney();
            case 8 -> viewTransactionHistory();
            case 9 -> searchAndFilterTransactions();
            case 10 -> menu.printLine(reportService.generateFullReport());
            case 11 -> closeAccount();
            case 12 -> processBulkTransactionsDemo();
            case 13 -> saveData();
            case 0 -> {
                return false;
            }
            default -> menu.printLine("Invalid choice. Please select an option from the menu.");
        }
        return true;
    }

    // ---------------------------------------------------------------- Customers

    private void addCustomer() throws BankException {
        String name = menu.readLine("Name: ");
        String email = menu.readLine("Email: ");
        String phone = menu.readLine("Phone (10 digits): ");
        Customer customer = customerService.addCustomer(name, email, phone);
        menu.printLine("Customer created successfully:\n" + customer);
    }

    private void viewCustomers() {
        var customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            menu.printLine("No customers found.");
            return;
        }
        customers.forEach(c -> menu.printLine(c.toString()));
    }

    // ----------------------------------------------------------------- Accounts

    private void openAccount() throws BankException {
        String customerId = menu.readLine("Customer ID: ");
        String typeInput = menu.readLine("Account Type (SAVINGS / CURRENT): ").toUpperCase();
        AccountType type;
        try {
            type = AccountType.valueOf(typeInput);
        } catch (IllegalArgumentException e) {
            menu.printError("Invalid account type. Must be SAVINGS or CURRENT.");
            return;
        }
        BigDecimal openingBalance = menu.readAmount("Opening Balance: ");
        Account account = accountService.openAccount(customerId, type, openingBalance);
        menu.printLine("Account opened successfully:\n" + account);
    }

    private void viewAccount() throws BankException {
        String accountNumber = menu.readLine("Account Number: ");
        Account account = accountService.findAccount(accountNumber);
        menu.printLine(account.toString());
    }

    private void closeAccount() throws BankException {
        String accountNumber = menu.readLine("Account Number to close: ");
        accountService.closeAccount(accountNumber);
        menu.printLine("Account " + accountNumber + " closed successfully.");
    }

    // -------------------------------------------------------------- Transactions

    private void depositMoney() throws BankException {
        String accountNumber = menu.readLine("Account Number: ");
        BigDecimal amount = menu.readAmount("Amount to deposit: ");
        Transaction transaction = transactionService.deposit(accountNumber, amount);
        menu.printLine("Deposit successful:\n" + transaction);
    }

    private void withdrawMoney() throws BankException {
        String accountNumber = menu.readLine("Account Number: ");
        BigDecimal amount = menu.readAmount("Amount to withdraw: ");
        Transaction transaction = transactionService.withdraw(accountNumber, amount);
        menu.printLine("Withdrawal successful:\n" + transaction);
    }

    private void transferMoney() throws BankException {
        String from = menu.readLine("From Account Number: ");
        String to = menu.readLine("To Account Number: ");
        BigDecimal amount = menu.readAmount("Amount to transfer: ");
        Transaction transaction = transactionService.transfer(from, to, amount);
        menu.printLine("Transfer successful:\n" + transaction);
    }

    private void viewTransactionHistory() throws BankException {
        String scopeChoice = menu.readLine("View (A)ll transactions or (O)ne account's transactions? [A/O]: ");
        List<Transaction> transactions;
        if (scopeChoice.equalsIgnoreCase("O")) {
            String accountNumber = menu.readLine("Account Number: ");
            accountService.findAccount(accountNumber); // validates existence
            transactions = transactionService.getTransactionsForAccount(accountNumber);
        } else {
            transactions = transactionService.getAllTransactions();
        }
        printTransactions(transactions);
    }

    // ---------------------------------------------------------- Search & Filter

    private void searchAndFilterTransactions() throws BankException {
        menu.printLine("""
                  1. Find transactions above an amount
                  2. Find successful transactions
                  3. Find failed transactions
                  4. Find deposits
                  5. Find withdrawals
                  6. Find transfers
                  7. Sort by amount (highest first)
                  8. Sort by date (most recent first)
                  9. Find the largest transaction
                 10. Total amount transferred
                 11. Group transactions by type
                 12. Group transactions by date
                 13. Find transaction by ID""");
        int option = menu.readInt("Choose an option: ");
        switch (option) {
            case 1 -> printTransactions(searchService.findAboveAmount(menu.readAmount("Amount threshold: ")));
            case 2 -> printTransactions(searchService.findByStatus(TransactionStatus.SUCCESS));
            case 3 -> printTransactions(searchService.findByStatus(TransactionStatus.FAILED));
            case 4 -> printTransactions(searchService.findByType(TransactionType.DEPOSIT));
            case 5 -> printTransactions(searchService.findByType(TransactionType.WITHDRAWAL));
            case 6 -> printTransactions(searchService.findByType(TransactionType.TRANSFER));
            case 7 -> printTransactions(searchService.sortByAmount(false));
            case 8 -> printTransactions(searchService.sortByDate(false));
            case 9 -> searchService.findLargestTransaction()
                    .ifPresentOrElse(t -> menu.printLine(t.toString()), () -> menu.printLine("No transactions yet."));
            case 10 -> menu.printLine("Total transferred: " + searchService.totalTransferredAmount());
            case 11 -> searchService.groupByType().forEach((type, list) -> {
                menu.printLine("\n" + type + " (" + list.size() + "):");
                list.forEach(t -> menu.printLine("  " + t));
            });
            case 12 -> searchService.groupByDate().forEach((date, list) -> {
                menu.printLine("\n" + date + " (" + list.size() + "):");
                list.forEach(t -> menu.printLine("  " + t));
            });
            case 13 -> menu.printLine(transactionService.findTransaction(menu.readLine("Transaction ID: ")).toString());
            default -> menu.printLine("Invalid option.");
        }
    }

    private void printTransactions(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            menu.printLine("No matching transactions found.");
            return;
        }
        transactions.forEach(t -> menu.printLine(t.toString()));
    }

    // -------------------------------------------------------------- Multithreading

    private void processBulkTransactionsDemo() {
        menu.printLine("""
                This demo submits several deposit/withdrawal/transfer requests
                concurrently to prove that account balances stay correct even
                when multiple threads operate on the same accounts at once.""");

        String accountNumber = menu.readLine("Enter an existing account number to run the demo against: ");
        int count = menu.readInt("How many concurrent deposits of 100 should be submitted?: ");

        List<TransactionRequest> requests = java.util.stream.Stream
                .generate(() -> (TransactionRequest) new TransactionRequest.DepositRequest(accountNumber, BigDecimal.valueOf(100)))
                .limit(count)
                .toList();

        long start = System.currentTimeMillis();
        List<TransactionRequestResult> results = batchProcessor.processAll(requests, Math.min(8, Math.max(2, count)));
        long elapsed = System.currentTimeMillis() - start;

        long successCount = results.stream().filter(TransactionRequestResult::success).count();
        menu.printLine("Processed %d requests in %d ms (%d succeeded, %d failed)."
                .formatted(results.size(), elapsed, successCount, results.size() - successCount));

        try {
            BigDecimal finalBalance = accountService.getBalance(accountNumber);
            menu.printLine("Final balance for " + accountNumber + " = " + finalBalance
                    + " (expected exactly " + successCount * 100 + " added if starting balance is known).");
        } catch (BankException e) {
            menu.printError(e.getMessage());
        }
    }

    // ---------------------------------------------------------------------- Save

    private void saveData() {
        try {
            var path = FileExporter.exportSnapshot(
                    customerService.getAllCustomers(),
                    accountService.getAllAccounts(),
                    transactionService.getAllTransactions(),
                    "output");
            menu.printLine("Data saved to: " + path.toAbsolutePath());
        } catch (Exception e) {
            menu.printError("Failed to save data: " + e.getMessage());
        }
    }
}
