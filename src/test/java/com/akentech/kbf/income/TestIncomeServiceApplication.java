package com.akentech.kbf.income;

import org.springframework.boot.SpringApplication;

public class TestIncomeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(IncomeServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
