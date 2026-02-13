package com.banksystem.transaction.service;

import com.banksystem.transaction.dto.*;
import com.banksystem.transaction.model.Transaction;
import com.banksystem.transaction.model.TransactionStatus;
import com.banksystem.transaction.repository.TransactionRepository;
import com.banksystem.transaction.client.AccountClient;
import com.banksystem.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private static final String IDEMPOTENCY_KEY = "idem-123";

    @Test
    void deposit_shouldReturnExistingTransaction_whenIdempotencyKeyAlreadyExists() {
        Transaction existing = new Transaction(UUID.randomUUID(), "acc-1", null, BigDecimal.TEN,
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS,
                LocalDateTime.now(), IDEMPOTENCY_KEY
        );

        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(existing));

        DepositRequest request = new DepositRequest();
        request.setAccountId("acc-1");
        request.setAmount(BigDecimal.TEN);

        TransactionResponse result = transactionService.deposit(request, IDEMPOTENCY_KEY);

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrow_whenAmountIsNull() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        DepositRequest request = new DepositRequest();
        request.setAccountId("acc-1");
        request.setAmount(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(request, IDEMPOTENCY_KEY)
        );

        assertTrue(ex.getMessage().contains("positive"));
        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deposit_shouldThrow_whenAmountIsZeroOrNegative() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        DepositRequest request = new DepositRequest();
        request.setAccountId("acc-1");
        request.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> transactionService.deposit(request, IDEMPOTENCY_KEY));

        request.setAmount(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> transactionService.deposit(request, IDEMPOTENCY_KEY));

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deposit_shouldCreditAndSetSuccess_whenValid() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DepositRequest request = new DepositRequest();
        request.setAccountId("acc-1");
        request.setAmount(new BigDecimal("100.00"));

        TransactionResponse result = transactionService.deposit(request, IDEMPOTENCY_KEY);

        assertNotNull(result);
        assertEquals("acc-1", result.getAccountId());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(accountClient).credit(eq("acc-1"), argThat(tr ->
                tr != null && new BigDecimal("100.00").compareTo(tr.getAmount()) == 0
        ));

        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldSetFailedAndRethrow_whenClientThrows() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("downstream error"))
                .when(accountClient).credit(eq("acc-1"), any(TransactionRequest.class));

        DepositRequest request = new DepositRequest();
        request.setAccountId("acc-1");
        request.setAmount(new BigDecimal("50.00"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.deposit(request, IDEMPOTENCY_KEY)
        );

        assertTrue(ex.getMessage().contains("downstream"));

        verify(transactionRepository, atLeastOnce()).save(transactionCaptor.capture());
        Transaction lastSaved = transactionCaptor.getAllValues().get(transactionCaptor.getAllValues().size() - 1);

        assertEquals(TransactionStatus.FAILED, lastSaved.getStatus());

        verify(accountClient, times(1)).credit(eq("acc-1"), any(TransactionRequest.class));
    }

    @Test
    void withdraw_shouldReturnExistingTransaction_whenIdempotencyKeyAlreadyExists() {
        Transaction existing = new Transaction(
                UUID.randomUUID(), "acc-1", null, BigDecimal.ONE,
                TransactionType.WITHDRAW, TransactionStatus.SUCCESS,
                LocalDateTime.now(), IDEMPOTENCY_KEY
        );

        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(existing));

        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId("acc-1");
        request.setAmount(BigDecimal.ONE);

        TransactionResponse result = transactionService.withdraw(request, IDEMPOTENCY_KEY);

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_shouldThrow_whenAmountInvalid() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId("acc-1");

        request.setAmount(null);
        assertThrows(IllegalArgumentException.class, () -> transactionService.withdraw(request, IDEMPOTENCY_KEY));

        request.setAmount(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> transactionService.withdraw(request, IDEMPOTENCY_KEY));

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientFunds() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1"))
                .thenReturn(new BigDecimal("10.00"));

        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId("acc-1");
        request.setAmount(new BigDecimal("50.00"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(request, IDEMPOTENCY_KEY)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient"));
        verify(transactionRepository, never()).save(any());
        verify(accountClient, times(1)).getBalance("acc-1");
        verify(accountClient, never()).debit(anyString(), any());
    }

    @Test
    void withdraw_shouldDebitAndSetSuccess_whenValid() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1"))
                .thenReturn(new BigDecimal("200.00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId("acc-1");
        request.setAmount(new BigDecimal("50.00"));

        TransactionResponse result = transactionService.withdraw(request, IDEMPOTENCY_KEY);

        assertEquals(TransactionType.WITHDRAW, result.getType());
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());

        verify(accountClient).debit(eq("acc-1"), argThat(tr ->
                tr != null && new BigDecimal("50.00").compareTo(tr.getAmount()) == 0
        ));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldSetFailedAndRethrow_whenClientThrows() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1"))
                .thenReturn(new BigDecimal("200.00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("debit error"))
                .when(accountClient).debit(eq("acc-1"), any(TransactionRequest.class));

        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId("acc-1");
        request.setAmount(new BigDecimal("50.00"));

        assertThrows(RuntimeException.class, () -> transactionService.withdraw(request, IDEMPOTENCY_KEY));

        verify(transactionRepository, atLeastOnce()).save(transactionCaptor.capture());
        Transaction last = transactionCaptor.getAllValues().get(transactionCaptor.getAllValues().size() - 1);
        assertEquals(TransactionStatus.FAILED, last.getStatus());
    }

    @Test
    void transfer_shouldReturnExistingTransaction_whenIdempotencyKeyAlreadyExists() {
        Transaction existing = new Transaction(
                UUID.randomUUID(), "acc-1", "acc-2", BigDecimal.TEN,
                TransactionType.TRANSFER, TransactionStatus.SUCCESS,
                LocalDateTime.now(), IDEMPOTENCY_KEY
        );

        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(existing));

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-2");
        request.setAmount(BigDecimal.TEN);

        TransactionResponse result = transactionService.transfer(request, IDEMPOTENCY_KEY);

        assertNotNull(result);
        assertEquals(existing.getAccountId(), result.getAccountId());
        assertEquals(existing.getTargetAccountId(), result.getTargetAccountId());
        assertEquals(existing.getStatus(), result.getStatus());

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrow_whenAmountInvalid() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-2");

        request.setAmount(null);
        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(request, IDEMPOTENCY_KEY));

        request.setAmount(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(request, IDEMPOTENCY_KEY));

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrow_whenSourceEqualsTarget() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-1");
        request.setAmount(BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(request, IDEMPOTENCY_KEY));

        verifyNoInteractions(accountClient);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrow_whenInsufficientFunds() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1")).thenReturn(new BigDecimal("10.00"));

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-2");
        request.setAmount(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(request, IDEMPOTENCY_KEY));

        verify(accountClient, times(1)).getBalance("acc-1");
        verify(accountClient, never()).debit(anyString(), any());
        verify(accountClient, never()).credit(anyString(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_shouldDebitThenCreditAndSetSuccess_whenValid() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1")).thenReturn(new BigDecimal("1000.00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-2");
        request.setAmount(new BigDecimal("250.00"));

        TransactionResponse response = transactionService.transfer(request, IDEMPOTENCY_KEY);

        assertNotNull(response);
        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("250.00"), response.getAmount());

        InOrder inOrder = inOrder(accountClient);
        inOrder.verify(accountClient).debit(eq("acc-1"), any(TransactionRequest.class));
        inOrder.verify(accountClient).credit(eq("acc-2"), any(TransactionRequest.class));

        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldSetFailedAndRethrow_whenDebitFails_andNotCallCredit() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1")).thenReturn(new BigDecimal("1000.00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("debit failed"))
                .when(accountClient).debit(eq("acc-1"), any(TransactionRequest.class));

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-2");
        request.setAmount(new BigDecimal("250.00"));

        assertThrows(RuntimeException.class, () -> transactionService.transfer(request, IDEMPOTENCY_KEY));

        verify(accountClient, times(1)).debit(eq("acc-1"), any(TransactionRequest.class));
        verify(accountClient, never()).credit(anyString(), any());

        verify(transactionRepository, atLeastOnce()).save(transactionCaptor.capture());
        Transaction last = transactionCaptor.getAllValues().get(transactionCaptor.getAllValues().size() - 1);
        assertEquals(TransactionStatus.FAILED, last.getStatus());
    }

    @Test
    void transfer_shouldSetFailedAndRethrow_whenCreditFails_afterDebit() {
        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(accountClient.getBalance("acc-1")).thenReturn(new BigDecimal("1000.00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        doNothing().when(accountClient).debit(eq("acc-1"), any(TransactionRequest.class));
        doThrow(new RuntimeException("credit failed"))
                .when(accountClient).credit(eq("acc-2"), any(TransactionRequest.class));

        TransferRequest request = new TransferRequest();
        request.setAccountId("acc-1");
        request.setTargetAccountId("acc-2");
        request.setAmount(new BigDecimal("250.00"));

        assertThrows(RuntimeException.class, () -> transactionService.transfer(request, IDEMPOTENCY_KEY));

        verify(accountClient).debit(eq("acc-1"), any(TransactionRequest.class));
        verify(accountClient).credit(eq("acc-2"), any(TransactionRequest.class));

        verify(transactionRepository, atLeastOnce()).save(transactionCaptor.capture());
        Transaction last = transactionCaptor.getAllValues().get(transactionCaptor.getAllValues().size() - 1);
        assertEquals(TransactionStatus.FAILED, last.getStatus());
    }

    @Test
    void listByAccount_shouldFilterAndSortByCreatedAtDesc() {
        UUID id1 = UUID.randomUUID();
        Transaction t1 = new Transaction(id1, "acc-1", null, BigDecimal.ONE,
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS,
                LocalDateTime.now().minusMinutes(10), "k1");

        UUID id2 = UUID.randomUUID();
        Transaction t2 = new Transaction(id2, "acc-2", "acc-1", BigDecimal.ONE,
                TransactionType.TRANSFER, TransactionStatus.SUCCESS,
                LocalDateTime.now().minusMinutes(5), "k2");

        UUID id3 = UUID.randomUUID();
        Transaction t3 = new Transaction(id3, "acc-3", null, BigDecimal.ONE,
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS,
                LocalDateTime.now().minusMinutes(1), "k3");

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2, t3));

        List<TransactionResponse> result = transactionService.listByAccount("acc-1");

        assertEquals(2, result.size());
        assertEquals(id2, result.get(0).getId());
        assertEquals(id1, result.get(1).getId());
    }

    @Test
    void buildTransactionResponse_shouldMapFields() {
        Transaction tx = new Transaction(UUID.randomUUID(), "acc-1", "acc-2", new BigDecimal("10.00"),
                TransactionType.TRANSFER, TransactionStatus.SUCCESS,
                LocalDateTime.now(), "k99");

        TransactionResponse response = transactionService.buildTransactionResponse(tx);

        assertEquals(tx.getId(), response.getId());
        assertEquals(tx.getAccountId(), response.getAccountId());
        assertEquals(tx.getTargetAccountId(), response.getTargetAccountId());
        assertEquals(tx.getType(), response.getType());
        assertEquals(tx.getStatus(), response.getStatus());
        assertEquals(tx.getAmount(), response.getAmount());
        assertEquals(tx.getCreatedAt(), response.getCreatedAt());
    }
}