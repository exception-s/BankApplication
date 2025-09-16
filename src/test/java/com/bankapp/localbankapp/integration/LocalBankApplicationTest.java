package com.bankapp.localbankapp.integration;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.model.Currency;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.*;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class LocalBankApplicationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void fullBankingScenario() throws Exception {
		User newUser = new User();
		newUser.setUsername("integrationUser");
		newUser.setPassword("password123");
		newUser.setEmail("integration@test.com");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk());

		MvcResult authResult = mockMvc.perform(post("/api/auth/authenticate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"integrationUser","password":"password123"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String token = authResult.getResponse().getContentAsString();

		MvcResult accountResult = mockMvc.perform(post("/api/accounts")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId":"1","currency":"USD"}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode accountNode = objectMapper.readTree(accountResult.getResponse().getContentAsString());
		long accountId = accountNode.get("id").asLong();

		mockMvc.perform(get("/api/accounts/" + accountId + "/balance")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("0.00"));

		TransactionDTO deposit = new TransactionDTO(
				null,
				accountId,
				BigDecimal.valueOf(1000.00),
				Currency.USD,
				Currency.USD
		);
		mockMvc.perform(post("/api/transactions/deposit")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deposit)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/accounts/" + accountId + "/balance")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("1000.00"));

		TransactionDTO withdraw = new TransactionDTO(
				accountId,
				null,
				BigDecimal.valueOf(500.00),
				Currency.USD,
				Currency.USD
		);
		mockMvc.perform(post("/api/transactions/withdraw")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(withdraw)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/accounts/" + accountId + "/balance")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("500.00"));
	}
}
