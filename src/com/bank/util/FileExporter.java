package com.bank.util;

import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * Since this application intentionally has no database, "Save Data" writes a
 * plain-text snapshot of the current in-memory state to disk so the session's
 * results can be reviewed or archived outside the console.
 */
public final class FileExporter {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private FileExporter() {
    }

    public static Path exportSnapshot(Collection<Customer> customers,
                                       Collection<Account> accounts,
                                       List<Transaction> transactions,
                                       String outputDirectory) throws IOException {

        Path dir = Path.of(outputDirectory);
        Files.createDirectories(dir);

        Path file = dir.resolve("bank_snapshot_" + java.time.LocalDateTime.now().format(STAMP) + ".txt");

        StringBuilder sb = new StringBuilder();
        sb.append("=== CUSTOMERS ===\n");
        customers.forEach(c -> sb.append(c).append("\n"));

        sb.append("\n=== ACCOUNTS ===\n");
        accounts.forEach(a -> sb.append(a).append("\n"));

        sb.append("\n=== TRANSACTIONS ===\n");
        transactions.forEach(t -> sb.append(t).append("\n"));

        Files.writeString(file, sb.toString());
        return file;
    }
}
