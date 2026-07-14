package com.bank.model;

public record TransactionRequestResult(TransactionRequest request, boolean success, String message) {
}
