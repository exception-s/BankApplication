package com.BankApp.localbankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@PropertySource("classpath:application.yml")
@EnableJpaRepositories(basePackages = "com.BankApp.localbankapp.repository")
public class LocalBankAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(LocalBankAppApplication.class, args);
	}
}
