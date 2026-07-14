# Simple Banking Transaction Management System

A Core Java 17 console application for managing bank customers, accounts, and
transactions. No frameworks, no database — everything runs in memory for a
single console session, with an optional text-file snapshot export.

## How to run

Requires JDK 17 or later.

```bash
./build.sh   # compiles everything into build/
./run.sh     # builds (if needed) and launches the console app
```

Or manually:

```bash
find src -name "*.java" > sources.txt
javac -d build --release 17 @sources.txt
java -cp build com.bank.Main
```

## Project structure

```
src/com/bank/
├── Main.java                      Application entry point
│
├── model/                         Domain data types
│   ├── Customer.java               Record: customer identity + contact info
│   ├── Account.java                Sealed abstract base account
│   ├── SavingsAccount.java         Account subtype
│   ├── CurrentAccount.java         Account subtype
│   ├── AccountType.java            SAVINGS / CURRENT
│   ├── AccountStatus.java          ACTIVE / CLOSED
│   ├── Transaction.java            Record: immutable transaction log entry
│   ├── TransactionType.java        DEPOSIT / WITHDRAWAL / TRANSFER
│   ├── TransactionStatus.java      SUCCESS / FAILED
│   ├── TransactionRequest.java     Sealed interface for batch processing
│   └── TransactionRequestResult.java
│
├── exception/                     Custom checked exceptions
│   ├── BankException.java          Sealed base type
│   ├── CustomerNotFoundException.java
│   ├── AccountNotFoundException.java
│   ├── TransactionNotFoundException.java
│   ├── DuplicateCustomerException.java
│   ├── DuplicateAccountException.java
│   ├── InvalidAmountException.java
│   ├── InsufficientBalanceException.java
│   ├── AccountClosedException.java
│   └── InvalidTransferException.java
│
├── repository/                    Thread-safe in-memory storage
│   ├── CustomerRepository.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
│
├── service/                       Business logic
│   ├── CustomerService.java        Customer CRUD + lookups
│   ├── AccountService.java         Open/close/query accounts
│   ├── TransactionService.java     Deposit, withdraw, transfer (thread-safe)
│   ├── TransactionSearchService.java  Search, filter, sort, group
│   ├── ReportService.java          Bank-wide statistics and reports
│   └── BatchTransactionProcessor.java  Multi-threaded batch processing
│
├── util/                          Shared helpers
│   ├── IdGenerator.java            Thread-safe sequential ID generation
│   ├── Validator.java              Shared validation rules
│   └── FileExporter.java           "Save Data" text-file snapshot export
│
└── console/                        User interface
    ├── ConsoleMenu.java             Menu display + input parsing
    └── BankConsoleApp.java          Menu loop, wires services together
```

## Design notes

- **Layered architecture** — model → repository → service → console. Each
  layer only talks to the one directly below it, so business rules never
  leak into the UI and storage details never leak into business logic.
- **Thread safety** — every `Account` holds its own `ReentrantLock`.
  Deposits/withdrawals lock a single account; transfers lock both accounts
  involved, always in a fixed order (by account number) to prevent
  deadlocks between two threads transferring in opposite directions.
  `ConcurrentHashMap` and `CopyOnWriteArrayList` back the repositories.
- **Batch processing demo** (menu option 12) submits many requests through
  an `ExecutorService` / `CompletableFuture`, so you can watch account
  balances stay perfectly correct under real concurrent load.
- **Sealed types** — `Account`, `BankException`, and `TransactionRequest`
  are sealed to a known, closed set of subtypes, which is checked by the
  compiler wherever they're handled.
- **Modern Java 17 features used throughout**: records, sealed classes,
  pattern matching for `instanceof`, switch expressions, text blocks,
  `Optional`, streams/collectors, generics, and the `java.time` API.

## Feature checklist (maps to the assignment requirements)

| # | Requirement | Where |
|---|---|---|
| 1 | Customer management | `CustomerService`, menu options 1–2 |
| 2 | Account management | `AccountService`, menu options 3–4, 11 |
| 3 | Deposit | `TransactionService.deposit`, menu option 5 |
| 4 | Withdraw | `TransactionService.withdraw`, menu option 6 |
| 5 | Transfer | `TransactionService.transfer`, menu option 7 |
| 6 | Transaction history | `TransactionService` + `TransactionRepository`, menu option 8 |
| 7 | Search/filter/sort | `TransactionSearchService`, menu option 9 |
| 8 | Banking reports | `ReportService`, menu option 10 |
| 9 | Exception handling | `exception` package (sealed hierarchy) |
| 10 | Multithreading | `BatchTransactionProcessor`, per-account locks, menu option 12 |
| 11 | Console menu | `ConsoleMenu` + `BankConsoleApp` |
