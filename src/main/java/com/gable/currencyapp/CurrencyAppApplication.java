package com.gable.currencyapp;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class CurrencyAppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyAppApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Hello there");
	}
}
