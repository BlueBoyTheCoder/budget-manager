package com.example.budgetmanager.summary;

import com.example.budgetmanager.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Account, Long> {
}