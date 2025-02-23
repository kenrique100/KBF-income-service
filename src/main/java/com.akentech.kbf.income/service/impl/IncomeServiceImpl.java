package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.model.Income;
import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.utils.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.bson.types.ObjectId;

import java.math.BigDecimal;

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
        if (id == null || id.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null or empty"));
        }

        // Validate ObjectId format
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        LoggingUtil.logInfo("Fetching income by ID: " + id);

        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found with id: " + id)))
                .map(income -> {
                    income.calculateDueBalance(); // Ensure dueBalance is set
                    return income;
                })
                .onErrorResume(e -> {
                    LoggingUtil.logError("Error fetching income by ID: " + id + ", Error: " + e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<Income> createIncome(Income income) {
        LoggingUtil.logInfo("Creating new income: " + income.getReason());
        income.calculateDueBalance();
        return incomeRepository.save(income);
    }

    @Override
    public Mono<Income> updateIncome(String id, Income income) {
        if (id == null || id.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null or empty"));
        }

        // Validate ObjectId format
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        // Validate income fields
        if (income.getAmountReceived() == null || income.getAmountReceived().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount received must be positive"));
        }

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
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found with id: " + id)));
    }

    @Override
    public Mono<Void> deleteIncome(String id) {
        if (!ObjectId.isValid(id)) { // Validate MongoDB ObjectId format
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        return incomeRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));
                    }
                    return incomeRepository.deleteById(id);
                });
    }


}