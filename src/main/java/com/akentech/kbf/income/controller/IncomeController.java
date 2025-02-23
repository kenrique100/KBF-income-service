package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.model.Income;
import com.akentech.kbf.income.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public Flux<Income> getAllIncomes() {
        return incomeService.getAllIncomes();
    }

    @GetMapping("/{id}")
    public Mono<Income> getIncomeById(@PathVariable String id) {
        return incomeService.getIncomeById(id);
    }

    @PostMapping
    public Mono<Income> createIncome(@RequestBody Income income) {
        return incomeService.createIncome(income);
    }

    @PutMapping("/{id}")
    public Mono<Income> updateIncome(@PathVariable String id, @RequestBody Income income) {
        return incomeService.updateIncome(id, income);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteIncome(@PathVariable String id) {
        return incomeService.deleteIncome(id);
    }
}