package com.example.budgetmanager.transaction;

import com.example.budgetmanager.summary.CategoryOverviewDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByAccountId(Long accountId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = com.example.budgetmanager.transaction.TransactionType.INCOME")
    Optional<BigDecimal> sumAllIncome();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = com.example.budgetmanager.transaction.TransactionType.EXPENSE")
    Optional<BigDecimal> sumAllExpenses();

    @Query("SELECT new com.example.budgetmanager.summary.CategoryOverviewDto(t.category.name, SUM(t.amount)) " +
            "FROM Transaction t " +
            "WHERE t.type = com.example.budgetmanager.transaction.TransactionType.EXPENSE " +
            "GROUP BY t.category.name")
    List<CategoryOverviewDto> sumExpensesGroupedByCategory();


    @Query("SELECT t FROM Transaction t WHERE " +
            "(:fromDate IS NULL OR t.transactionDate >= :fromDate) AND " +
            "(:toDate IS NULL OR t.transactionDate <= :toDate) AND " +
            "(:categoryName IS NULL OR LOWER(t.category.name) = LOWER(:categoryName))")
    List<Transaction> findFilteredTransactions(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("categoryName") String categoryName
    );
}