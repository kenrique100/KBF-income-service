package com.akentech.kbf.income.exception;

public class IncomeNotFoundException extends RuntimeException {
    public IncomeNotFoundException(String message) {
        super(message);
    }
}