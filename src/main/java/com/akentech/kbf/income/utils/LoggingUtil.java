package com.akentech.kbf.income.utils;

public class LoggingUtil {
    public static void logInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void logError(String message) {
        System.err.println("[ERROR] " + message);
    }
}