package com.bank.model;

import java.math.BigDecimal;

/**
 * A pending unit of work for the multi-threaded batch processor.
 * Sealed to exactly three kinds of request, allowing the processor to
 * exhaustively pattern-match on the concrete request type.
 */
public sealed interface TransactionRequest {

    record DepositRequest(String accountNumber, BigDecimal amount) implements TransactionRequest {
    }

    record WithdrawRequest(String accountNumber, BigDecimal amount) implements TransactionRequest {
    }

    record TransferRequest(String fromAccountNumber, String toAccountNumber, BigDecimal amount)
            implements TransactionRequest {
    }
}
