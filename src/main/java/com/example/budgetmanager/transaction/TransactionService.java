package com.example.budgetmanager.transaction;

import com.example.budgetmanager.account.Account;
import com.example.budgetmanager.account.AccountRepository;
import com.example.budgetmanager.category.Category;
import com.example.budgetmanager.category.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public TransactionDto create(TransactionDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + dto.getAccountId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // Update Account Balance
        if (dto.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(dto.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(dto.getAmount()));
        }
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                null,
                dto.getAmount(),
                dto.getType(),
                category,
                dto.getDescription(),
                dto.getTransactionDate(),
                account
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        Account account = transaction.getAccount();

        // Revert Account Balance
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
                t.getAccount().getId()
        );
    }
}