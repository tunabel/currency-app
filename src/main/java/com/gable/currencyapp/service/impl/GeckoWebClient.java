package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.dto.gecko.CoinByCurrencyDto;
import com.gable.currencyapp.dto.gecko.CoinDetailsDto;
import com.gable.currencyapp.dto.gecko.SimpleCoinDto;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class GeckoWebClient {

  private static final String VS_CURRENCY = "vs_currency";
  private static final String PAGE = "page";
  private static final String PER_PAGE = "per_page";
  private final WebClient webClient;

  @Value("${currency-app.coin-gecko.path.simple-coin-list}")
  private String simpleCoinListPath;

  @Value("${currency-app.coin-gecko.path.coin-list}")
  private String coinListPath;

  @Value("${currency-app.coin-gecko.path.coin-details}")
  private String coinDetailsPath;

  @Value("${currency-app.coin-gecko.path.currency-list}")
  private String currencyListPath;

  public CoinDetailsDto queryCoinDetails(String coinId) {
    return webClient.get()
        .uri(coinDetailsPath + coinId)
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(CoinDetailsDto.class);
              } else {
                log.error("Unable to get data for coin id {}. Possibly limit reached", coinId);
                return Mono.empty();
              }
            }
        )
        .block();
  }

  public SimpleCoinDto[] querySimpleCoinList() {
    return webClient.get()
        .uri(simpleCoinListPath)
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(SimpleCoinDto[].class);
              } else {
                log.error("Unable to get simple coin list. Possibly limit reached");
                return Mono.empty();
              }
            }
        )
        .block();
  }

  public CoinByCurrencyDto[] queryCoinListByCurrencies(String currency, int page, int perPage) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder.path(coinListPath)
            .queryParam(VS_CURRENCY, currency)
            .queryParam(PAGE, page)
            .queryParam(PER_PAGE, perPage)
            .build()
        )
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(CoinByCurrencyDto[].class);
              } else {
                log.error("Unable to get data for currency {}. Possibly limit reached", currency);
                return Mono.empty();
              }
            }
        )
        .block();
  }

  public List<String> queryCurrencyList() {
     String[] currencyArray = webClient.get()
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

    if (currencyArray == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(currencyArray).collect(Collectors.toList());
  }
}
