package com.example.budgetmanager.account;

import com.example.budgetmanager.transaction.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(acc -> new AccountDto(acc.getId(), acc.getName(), acc.getBalance()))
                .toList();
    }

    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + id));
        return new AccountDto(account.getId(), account.getName(), account.getBalance());
    }

    public AccountDto createAccount(AccountDto dto) {
        Account account = new Account(null, dto.getName(), dto.getBalance());
        Account savedAccount = accountRepository.save(account);
        return new AccountDto(savedAccount.getId(), savedAccount.getName(), savedAccount.getBalance());
    }

    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot delete. Account not found with ID: " + id));

        if (transactionRepository.existsByAccountId(id)) {
            throw new IllegalStateException("Cannot delete account with active transactions.");
        }

        accountRepository.delete(account);
    }
}