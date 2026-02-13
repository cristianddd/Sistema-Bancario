package com.banksystem.transaction.model;

/**
 * Possible states of a transaction. A transaction begins in PENDING state and
 * transitions to SUCCESS on successful completion or FAILED on error.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}