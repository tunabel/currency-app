package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.CoinRepository;
import com.gable.currencyapp.repository.PriceRepository;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.util.CrawlingTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CrawlService {

  private final CoinRepository coinRepository;
  private final VsCurrencyRepository vsCurrencyRepository;

  private final PriceRepository priceRepository;
  private final ThreadPoolTaskScheduler taskScheduler;

  private final GeckoWebClient webClient;

  @Qualifier("startingPeriodicTrigger")
  private final PeriodicTrigger startingPeriodicTrigger;

  @Qualifier("restartedPeriodicTrigger")
  private final PeriodicTrigger restartedPeriodicTrigger;

  @Value("${currency-app.last-updated-period}")
  private double lastUpdatedPeriod;

  private ScheduledFuture<?> crawlingJob;

  @PostConstruct
  public void startCrawlingJob() {
    startCrawlingCurrencyList(startingPeriodicTrigger);
  }

  @SneakyThrows
  private void startCrawlingCurrencyList(PeriodicTrigger periodicTrigger) {
    crawlingJob = taskScheduler.schedule(
        new CrawlingTask(coinRepository, vsCurrencyRepository, priceRepository, log, webClient,
            lastUpdatedPeriod),
        periodicTrigger);
  }

  public void rescheduleCrawlingJob(String currency) {
    log.info("Restarting crawling task to fetch updated currency {}", currency);
    crawlingJob.cancel(true);
    startCrawlingCurrencyList(restartedPeriodicTrigger);
  }
}
