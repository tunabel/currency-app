package com.gable.currencyapp.util;

import com.gable.currencyapp.dto.gecko.CoinByCurrencyDto;
import com.gable.currencyapp.model.Coin;
import com.gable.currencyapp.model.Price;
import com.gable.currencyapp.model.PricePK;
import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.CoinRepository;
import com.gable.currencyapp.repository.PriceRepository;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.service.impl.GeckoWebClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class CrawlingTask implements Runnable {

  private final CoinRepository coinRepository;

  private final VsCurrencyRepository vsCurrencyRepository;

  private final PriceRepository priceRepository;
  private final Logger log;
  private final GeckoWebClient webClient;

  private final double lastUpdatedPeriod;

  @Override
  public void run() {

    int coinCount = 0;
    while (coinCount == 0) {
      coinCount = (int) coinRepository.count();
    }

    log.info("Start crawling coin data...");

    List<VsCurrency> vsCurrencyList = vsCurrencyRepository.findAllOrderByPriority();

    for (VsCurrency vsCurrency : vsCurrencyList) {
      String currency = vsCurrency.getCurrency();
      int count = 0;
      while (true) {
        log.info("Crawling coin data for currency {}. page {}", currency, 250 * (count + 1));
        CoinByCurrencyDto[] coinArray = webClient.queryCoinListByCurrencies(currency, count++, 250);
        if (coinArray.length == 0) {
          log.info("No more coin data available for currency {}. skip to next currency", currency);
          break;
        }
        List<Coin> coinList = new ArrayList<>();

        List<Coin> coinDbList = coinRepository.findCoinsByIdAndNotUpdated(
            Arrays.stream(coinArray).map(CoinByCurrencyDto::getId).collect(Collectors.toList()),
            lastUpdatedPeriod
        );

        for (CoinByCurrencyDto coinDto : coinArray) {
          Coin coin;
          String coinId = coinDto.getId();
          Optional<Coin> coinOptional = coinDbList.stream()
              .filter(coinDb -> coinDb.getId().equals(coinId))
              .findFirst();

          if (coinOptional.isPresent()) {
            coin = coinOptional.get();
          } else {
            coin = new Coin();
            coin.setId(coinId);
            coin.setName(coinDto.getName());
            coin.setSymbol(coinDto.getSymbol());
          }
          coin.setImage(coinDto.getImage());
          coin.setPriceChangePercent(coinDto.getPriceChangePercent());
          coin.setMarketCapRank(coinDto.getMarketCapRank());
          coin.setLastUpdatedTime(Instant.now());

          priceRepository.save(createPrice(vsCurrency, currency, coinDto, coin, coinId));
          coinList.add(coin);
        }
        coinRepository.saveAll(coinList);
      }

    }
  }

  private Price createPrice(VsCurrency vsCurrency, String currency, CoinByCurrencyDto coinDto,
      Coin coin, String coinId) {
    Price price;
    PricePK pricePK = new PricePK(coinId, currency);
    Optional<Price> optionalPrice = priceRepository.findPriceByPricePK(pricePK);
    if (optionalPrice.isPresent()) {
      price = optionalPrice.get();
    } else {
      price = new Price();
      price.setPricePK(pricePK);
      price.setCurrency(vsCurrency);
      price.setCoin(coin);
    }
    price.setCurrentPrice(coinDto.getCurrentPrice());
    return price;
  }
}
