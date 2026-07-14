package com.bank.service;

import com.bank.exception.BankException;
import com.bank.model.TransactionRequest;
import com.bank.model.TransactionRequest.DepositRequest;
import com.bank.model.TransactionRequest.TransferRequest;
import com.bank.model.TransactionRequest.WithdrawRequest;
import com.bank.model.TransactionRequestResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Processes a batch of deposit/withdrawal/transfer requests concurrently using a
 * fixed thread pool. Correctness under concurrency is guaranteed by the per-account
 * locking implemented in Account/TransactionService, not by this class - this class
 * only fans work out and collects results back in with CompletableFuture.
 */
public class BatchTransactionProcessor {

    private final TransactionService transactionService;

    public BatchTransactionProcessor(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public List<TransactionRequestResult> processAll(List<TransactionRequest> requests, int threadPoolSize) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            List<CompletableFuture<TransactionRequestResult>> futures = requests.stream()
                    .map(request -> CompletableFuture.supplyAsync(() -> processOne(request), executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private TransactionRequestResult processOne(TransactionRequest request) {
        try {
            // instanceof pattern matching dispatches to the correct operation
            // based on the concrete sealed-interface implementation.
            if (request instanceof DepositRequest r) {
                transactionService.deposit(r.accountNumber(), r.amount());
            } else if (request instanceof WithdrawRequest r) {
                transactionService.withdraw(r.accountNumber(), r.amount());
            } else if (request instanceof TransferRequest r) {
                transactionService.transfer(r.fromAccountNumber(), r.toAccountNumber(), r.amount());
            }
            return new TransactionRequestResult(request, true, "Processed successfully on thread "
                    + Thread.currentThread().getName());
        } catch (BankException e) {
            return new TransactionRequestResult(request, false, e.getMessage());
        }
    }
}
