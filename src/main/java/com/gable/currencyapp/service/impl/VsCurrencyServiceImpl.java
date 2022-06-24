package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.service.VsCurrencyService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class VsCurrencyServiceImpl implements VsCurrencyService {

  private final VsCurrencyRepository vsCurrencyRepository;
  private final WebClient webClient;

  @Override
  public void retrieveCurrencies() {
    log.info("Querying into coingecko to get VsCurrencies");
    Mono<String[]> currenciesArrMono = webClient.get()
        .uri("/simple/supported_vs_currencies")
        .retrieve()
        .bodyToMono(String[].class);

    String[] currenciesArray = currenciesArrMono.block();
    if (currenciesArray == null) {
      log.error("Unable to get currencies from coingecko");
      return;
    }

    List<String> currencyStringList = Arrays.stream(currenciesArray).collect(Collectors.toList());
    List<VsCurrency> dbCurrencies = vsCurrencyRepository.findAll();
    if (!dbCurrencies.isEmpty() && dbCurrencies.size() == currencyStringList.size()) {
      log.info("Currencies already updated");
      return;
    }

    List<VsCurrency> newCurrencies = currencyStringList.stream()
        .map(VsCurrency::new)
        .collect(Collectors.toList());

    vsCurrencyRepository.saveAll(newCurrencies);
    log.info("Currencies updated");
  }
}
