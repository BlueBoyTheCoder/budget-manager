package com.example.budgetmanager.transaction;

import com.example.budgetmanager.account.Account;
import com.example.budgetmanager.account.AccountRepository;
import com.example.budgetmanager.category.Category;
import com.example.budgetmanager.category.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<TransactionDto> getFilteredTransactions(LocalDate from, LocalDate to, String categoryName) {
        return transactionRepository.findFilteredTransactions(from, to, categoryName).stream()
                .map(this::mapToDto)
                .toList();
    }

    public TransactionDto create(TransactionDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + dto.getAccountId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));

        if (dto.getType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(dto.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(dto.getAmount()));
        }
        accountRepository.save(account);

        String warning = null;
        if (dto.getType() == TransactionType.EXPENSE && category.getBudgetLimit() != null) {

            LocalDate transactionDate = dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDate.now();

            LocalDate startOfTargetMonth = transactionDate.withDayOfMonth(1);
            LocalDate endOfTargetMonth = transactionDate.withDayOfMonth(transactionDate.lengthOfMonth());

            BigDecimal targetMonthExpenses = transactionRepository
                    .sumExpensesByCategoryIdAndDateRange(category.getId(), startOfTargetMonth, endOfTargetMonth);

            BigDecimal totalWithNewTransaction = targetMonthExpenses.add(dto.getAmount());

            if (totalWithNewTransaction.compareTo(category.getBudgetLimit()) > 0) {
                warning = String.format("Warning: Budget limit exceeded for category '%s' in %s %s! Limit: %s, Total with this transaction: %s",
                        category.getName(),
                        transactionDate.getMonth(),
                        transactionDate.getYear(),
                        category.getBudgetLimit(),
                        totalWithNewTransaction);
            }
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDate.now());
        transaction.setAccount(account);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionDto(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getCategory().getId(),
                savedTransaction.getDescription(),
                savedTransaction.getTransactionDate(),
                savedTransaction.getAccount().getId(),
                warning
        );
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        Account account = transaction.getAccount();

        if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }

    private TransactionDto mapToDto(Transaction t) {
        return new TransactionDto(
                t.getId(),
                t.getAmount(),
                t.getType(),
                t.getCategory().getId(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getAccount().getId(),
                null
        );
    }
}