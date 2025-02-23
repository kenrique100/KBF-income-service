package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.exception.IncomeNotFoundException;
import com.akentech.kbf.income.model.Income;
import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.utils.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    public Flux<Income> getAllIncomes() {
        LoggingUtil.logInfo("Fetching all incomes");
        return incomeRepository.findAll();
    }

    @Override
    public Mono<Income> getIncomeById(String id) {
        LoggingUtil.logInfo("Fetching income by ID: " + id);
        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new IncomeNotFoundException("Income not found with id: " + id)))
                .onErrorResume(e -> {
                    LoggingUtil.logError("Error fetching income by ID: " + id + ", Error: " + e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<Income> createIncome(Income income) {
        LoggingUtil.logInfo("Creating new income: " + income.getReason());
        income.calculateDueBalance(); // Calculate due balance before saving
        return incomeRepository.save(income);
    }

    @Override
    public Mono<Income> updateIncome(String id, Income income) {
        LoggingUtil.logInfo("Updating income with ID: " + id);
        return incomeRepository.findById(id)
                .flatMap(existingIncome -> {
                    existingIncome.setReason(income.getReason());
                    existingIncome.setIncomeDate(income.getIncomeDate());
                    existingIncome.setQuantity(income.getQuantity());
                    existingIncome.setAmountReceived(income.getAmountReceived());
                    existingIncome.setExpectedAmount(income.getExpectedAmount());
                    existingIncome.calculateDueBalance(); // Recalculate due balance
                    existingIncome.setReceipt(income.getReceipt());
                    existingIncome.setCreatedBy(income.getCreatedBy());
                    return incomeRepository.save(existingIncome);
                })
                .switchIfEmpty(Mono.error(new IncomeNotFoundException("Income not found with id: " + id)));
    }

    @Override
    public Mono<Void> deleteIncome(String id) {
        LoggingUtil.logInfo("Deleting income with ID: " + id);
        return incomeRepository.deleteById(id);
    }
}