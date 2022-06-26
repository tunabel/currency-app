package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.service.VsCurrencyService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class VsCurrencyServiceImpl implements VsCurrencyService {

  private final VsCurrencyRepository vsCurrencyRepository;
  private final WebClient webClient;

  @Value("${currency-app.coin-gecko.path.currency-list}")
  private String currencyListPath;

  @Value("${currency-app.crawl.prioritized-currency}")
  private List<String> defaultPrioritizedCurrency;

  @Override
  public void retrieveCurrencies() {
    List<String> currencyStringList = queryCurrencyListFromServer();

    if (currencyStringList.isEmpty()) {
      return;
    }

    List<VsCurrency> dbCurrencies = vsCurrencyRepository.findAll();
    if (!dbCurrencies.isEmpty() && dbCurrencies.size() == currencyStringList.size()) {
      log.info("Currencies already populated");
      return;
    }

    List<VsCurrency> newCurrencies = currencyStringList.stream()
        .map(currency -> {
          VsCurrency vsCurrency = new VsCurrency();
          vsCurrency.setCurrency(currency.toLowerCase(Locale.ROOT));

          //automatically prioritize currency
          if (defaultPrioritizedCurrency.contains(currency)) {
            vsCurrency.setCrawlPriority((byte) defaultPrioritizedCurrency.indexOf(currency));
          }

          return vsCurrency;
        })
        .collect(Collectors.toList());

    vsCurrencyRepository.saveAll(newCurrencies);
    log.info("{} Currencies updated", newCurrencies.size());
  }

  private List<String> queryCurrencyListFromServer() {
    log.info("Querying into coin-gecko to get currency list");
    String[] currenciesArray = webClient.get()
        .uri(currencyListPath)
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(String[].class);
              } else {
                log.error("Unable to get currency list. Possibly limit reached");
                return Mono.empty();
              }
            }
        )
        .block();

    if (currenciesArray == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(currenciesArray).collect(Collectors.toList());
  }

  @Override
  public VsCurrency updateVsCurrencyPriority(String currency) {
    VsCurrency updatedCurrency = vsCurrencyRepository.getReferenceById(currency);

    if (Objects.equals(updatedCurrency.getCrawlPriority(), 0)) {
      return updatedCurrency;
    }

    List<VsCurrency> vsCurrencyList = vsCurrencyRepository.findAllWithPriority();

    vsCurrencyList.removeIf(vsCur -> vsCurrencyList.contains(updatedCurrency));
    vsCurrencyList.add(0, updatedCurrency);

    for (VsCurrency vsCurrency : vsCurrencyList) {
      vsCurrency.setCrawlPriority((byte) (vsCurrencyList.indexOf(vsCurrency)));
    }
    vsCurrencyRepository.saveAll(vsCurrencyList);
    return updatedCurrency;
  }
}
