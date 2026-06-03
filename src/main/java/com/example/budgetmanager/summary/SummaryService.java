package com.example.budgetmanager.summary;

import com.example.budgetmanager.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final TransactionRepository transactionRepository;

    public SummaryDto getSummary() {
        return new SummaryDto(transactionRepository.sumAllIncome().orElse(BigDecimal.ZERO),
                transactionRepository.sumAllExpenses().orElse(BigDecimal.ZERO),
                transactionRepository.sumExpensesGroupedByCategory());
    }

}