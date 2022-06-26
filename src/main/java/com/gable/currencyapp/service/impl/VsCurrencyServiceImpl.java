package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.exception.ServerInternalErrorException;
import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.service.VsCurrencyService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class VsCurrencyServiceImpl implements VsCurrencyService {

  private final VsCurrencyRepository vsCurrencyRepository;
  private final GeckoWebClient webClient;

  private final CrawlService crawlService;

  @Value("${currency-app.crawl.prioritized-currency}")
  private List<String> defaultPrioritizedCurrency;

  @Override
  public void retrieveCurrencies() {
    List<String> currencyStringList = webClient.queryCurrencyList();
    List<VsCurrency> dbCurrencies = vsCurrencyRepository.findAll();

    if (currencyStringList.isEmpty() && dbCurrencies.isEmpty()) {
      throw new ServerInternalErrorException(
          "CoinGecko server is down. Please try restarting the server later");
    }

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

  @Override
  public VsCurrency updateVsCurrencyPriority(String currency) {
    VsCurrency updatedCurrency = vsCurrencyRepository.getReferenceById(currency);

    if (updatedCurrency.getCrawlPriority() != null && updatedCurrency.getCrawlPriority() == 0) {
      return updatedCurrency;
    }

    List<VsCurrency> vsCurrencyList = vsCurrencyRepository.findAllWithPriority();
    vsCurrencyList.remove(updatedCurrency);
    vsCurrencyList.add(0, updatedCurrency);

    for (VsCurrency vsCurrency : vsCurrencyList) {
      vsCurrency.setCrawlPriority((byte) (vsCurrencyList.indexOf(vsCurrency)));
    }
    vsCurrencyRepository.saveAll(vsCurrencyList);

    crawlService.rescheduleCrawlingJob(currency);

    return updatedCurrency;
  }
}
