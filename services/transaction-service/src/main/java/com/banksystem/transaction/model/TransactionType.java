package com.banksystem.transaction.model;

/**
 * Enumerates the supported transaction types. A DEPOSIT credits the given
 * account, WITHDRAW debits the account, and TRANSFER debits the source account
 * and credits the target account.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}