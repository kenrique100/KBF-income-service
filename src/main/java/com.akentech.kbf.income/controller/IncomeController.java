package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.model.Income;
import com.akentech.kbf.income.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j // Enables logging using SLF4J
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public Flux<Income> getAllIncomes() {
        log.info("Received GET request for all incomes");
        return incomeService.getAllIncomes()
                .doOnNext(income -> log.info("Returning income: {}", income));
    }

    @GetMapping("/{id}")
    public Mono<Income> getIncomeById(@PathVariable String id) {
        log.info("Received GET request for income with ID: {}", id);
        return incomeService.getIncomeById(id)
                .doOnSuccess(income -> log.info("Returning income with Id: {}", income))
                .doOnError(error -> log.error("Error fetching income with ID {}: {}", id, error.getMessage()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Income> createIncome(@RequestBody @Valid Income income) {
        log.info("Received POST request to create income: {}", income);
        return incomeService.createIncome(income)
                .doOnSuccess(savedIncome -> log.info("Income created successfully: {}", savedIncome))
                .doOnError(error -> log.error("Error creating income: {}", error.getMessage()));
    }

    @PutMapping("/{id}")
    public Mono<Income> updateIncome(@PathVariable String id, @RequestBody Income income) {
        log.info("Received PUT request to update income with ID: {}", id);
        return incomeService.updateIncome(id, income)
                .doOnSuccess(updatedIncome -> log.info("Income updated successfully: {}", updatedIncome))
                .doOnError(error -> log.error("Error updating income with ID {}: {}", id, error.getMessage()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIncome(@PathVariable String id) {
        log.info("Received DELETE request for income with ID: {}", id);
        return incomeService.deleteIncome(id)
                .doOnSuccess(v -> log.info("Income with ID {} deleted successfully", id))
                .doOnError(error -> log.error("Error deleting income with ID {}: {}", id, error.getMessage()));
    }
}