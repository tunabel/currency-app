package com.gable.currencyapp;

import com.gable.currencyapp.service.VsCurrencyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class CurrencyAppApplication implements CommandLineRunner {

	private final VsCurrencyService vsCurrencyService;

	public CurrencyAppApplication(VsCurrencyService vsCurrencyService) {
		this.vsCurrencyService = vsCurrencyService;
	}

	public static void main(String[] args) {
		SpringApplication.run(CurrencyAppApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Starting app");
		vsCurrencyService.retrieveCurrencies();
	}
}
