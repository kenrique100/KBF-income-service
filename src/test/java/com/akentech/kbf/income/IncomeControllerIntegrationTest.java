package com.akentech.kbf.income;

import com.akentech.kbf.income.model.Income;
import com.akentech.kbf.income.repository.IncomeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class IncomeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IncomeRepository incomeRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        incomeRepository.deleteAll().block();
    }

    @AfterEach
    void tearDown() {
        // Clear the database after each test
        incomeRepository.deleteAll().block();
    }

    @Test
    void testCreateIncome() {
        Income income = Income.builder()
                .reason("Freelance Work")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(500))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        webTestClient.post()
                .uri("/api/incomes")
                .body(Mono.just(income), Income.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Freelance Work")
                .jsonPath("$.dueBalance").isEqualTo(500.00);
    }

    @Test
    void testGetIncomeById() {
        Income income = Income.builder()
                .reason("Project Completion")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(1000))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt456")
                .createdBy("Jane Doe")
                .build();

        income.calculateDueBalance(); // Ensure dueBalance is set before saving

        Income savedIncome = incomeRepository.save(income).block();

        assert savedIncome != null;
        webTestClient.get()
                .uri("/api/incomes/" + savedIncome.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Project Completion")
                .jsonPath("$.dueBalance").exists() // Ensure the field is present
                .jsonPath("$.dueBalance").isEqualTo(0.00); // Verify the correct value
    }


    @Test
    void testUpdateIncome() {
        Income income = Income.builder()
                .reason("Freelance Work")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(500))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        Income savedIncome = incomeRepository.save(income).block();

        Income updatedIncome = Income.builder()
                .reason("Updated Freelance Work")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(700))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        assert savedIncome != null;
        webTestClient.put()
                .uri("/api/incomes/" + savedIncome.getId())
                .body(Mono.just(updatedIncome), Income.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Updated Freelance Work")
                .jsonPath("$.dueBalance").isEqualTo(300.00);
    }

    @Test
    void testDeleteIncome() {
        Income income = Income.builder()
                .reason("Project Completion")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(1000))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt456")
                .createdBy("Jane Doe")
                .build();

        Income savedIncome = incomeRepository.save(income).block();

        assert savedIncome != null;

        webTestClient.delete()
                .uri("/api/incomes/" + savedIncome.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify that the income was actually deleted
        webTestClient.get()
                .uri("/api/incomes/" + savedIncome.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetAllIncomes() {
        Income income1 = Income.builder()
                .reason("Freelance Work")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(500))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt123")
                .createdBy("John Doe")
                .build();

        Income income2 = Income.builder()
                .reason("Project Completion")
                .incomeDate(LocalDate.now())
                .quantity(1)
                .amountReceived(BigDecimal.valueOf(1000))
                .expectedAmount(BigDecimal.valueOf(1000))
                .receipt("receipt456")
                .createdBy("Jane Doe")
                .build();

        incomeRepository.save(income1).block();
        incomeRepository.save(income2).block();

        webTestClient.get()
                .uri("/api/incomes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Income.class)
                .hasSize(2);
    }
}