package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.dto.app.ResponseCoinDto;
import com.gable.currencyapp.dto.gecko.CoinByCurrencyDto;
import com.gable.currencyapp.dto.gecko.CoinDetailsDto;
import com.gable.currencyapp.dto.gecko.SimpleCoinDto;
import com.gable.currencyapp.model.Coin;
import com.gable.currencyapp.model.Price;
import com.gable.currencyapp.model.PricePK;
import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.CoinRepository;
import com.gable.currencyapp.repository.PriceRepository;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.repository.projection.CoinWithPrice;
import com.gable.currencyapp.service.CoinService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

  private final CoinRepository coinRepository;
  private final PriceRepository priceRepository;

  private final VsCurrencyRepository vsCurrencyRepository;

  private final GeckoWebClient webClient;

  @Value("${currency-app.last-updated-period}")
  private double lastUpdatedPeriod;

  @Override
  public void retrieveCoinList() {
    if (coinRepository.count() > 0) {
      log.info("Coin list already has data. Won't query data again");
      return;
    }

    log.info("Querying into coin-gecko to get simple coin list");
    SimpleCoinDto[] coinArray = webClient.querySimpleCoinList();

    if (coinArray == null) {
      return;
    }

    List<SimpleCoinDto> coinList = Arrays.stream(coinArray).collect(Collectors.toList());
    List<Coin> coinToDb = coinList.stream()
        .map(
            coinDto -> {
              Coin coin = new Coin();
              coin.setId(coinDto.getId());
              coin.setSymbol(coinDto.getSymbol());
              coin.setName(coinDto.getName());
              return coin;
            })
        .collect(Collectors.toList());

    coinRepository.saveAll(coinToDb);
    log.info("{} coins saved", coinToDb.size());
  }

  @Override
  public List<ResponseCoinDto> getCoinResponseDtosByCurrency(VsCurrency currency, int page, int perPage) {
    log.info("Get list of coins by currency {}", currency.getCurrency());
    CoinByCurrencyDto[] coinList = webClient.queryCoinListByCurrencies(currency.getCurrency(), page, perPage);

    if (coinList == null) {
      log.info("Unable to get coin list by currency {}", currency);
      return new ArrayList<>();
    }

    List<ResponseCoinDto> responseCoinDtos = createResponseCoinDtos(coinList, currency.getCurrency());

    cacheCoinDataIntoDb(responseCoinDtos, currency);

    return responseCoinDtos;
  }

  public List<ResponseCoinDto> createResponseCoinDtos(CoinByCurrencyDto[] coinList, String currency) {
    List<String> coinIds = Arrays.stream(coinList)
        .map(CoinByCurrencyDto::getId)
        .collect(Collectors.toList());

    List<CoinWithPrice> recentCoinDataPriceList = coinRepository
        .findCoinDataByIdAndCurrencyAndLastUpdatedPeriod(coinIds, currency, lastUpdatedPeriod);

    List<ResponseCoinDto> responseCoinDtos = new ArrayList<>();

    for (CoinByCurrencyDto coinByCurrency : coinList) {
      ResponseCoinDto responseCoinDto;
      Optional<CoinWithPrice> matchCoinInDbOptional = recentCoinDataPriceList
          .stream()
          .filter(coinDb -> coinDb.getId().equals(coinByCurrency.getId()))
          .findFirst();

      responseCoinDto = matchCoinInDbOptional
          .map(this::createResponseCoinDtoFromDbCoin)
          .orElseGet(() -> createResponseCoinDtoFromServer(coinByCurrency));
      responseCoinDtos.add(responseCoinDto);
    }

    return responseCoinDtos;
  }

  private ResponseCoinDto createResponseCoinDtoFromDbCoin(CoinWithPrice coin) {
    log.info("Map data from database to response for coin {}", coin.getId());
    ResponseCoinDto responseCoinDto = new ResponseCoinDto();
    responseCoinDto.setId(coin.getId());
    responseCoinDto.setName(coin.getName());
    responseCoinDto.setImage(coin.getImage());
    responseCoinDto.setSymbol(coin.getSymbol());
    responseCoinDto.setDescription(coin.getDescription());
    responseCoinDto.setPriceChangePercent(coin.getPriceChangePercent());
    responseCoinDto.setCurrentPrice(coin.getCurrentPrice());
    responseCoinDto.setMarketCapRank(coin.getMarketCapRank());
    responseCoinDto.setTradeUrl(coin.getTradeUrl());
    return responseCoinDto;
  }

  private ResponseCoinDto createResponseCoinDtoFromServer(CoinByCurrencyDto coinItem) {
    log.info("Query data of coin {}", coinItem.getId());
    CoinDetailsDto coinDetailsDto = webClient.queryCoinDetails(coinItem.getId());

    ResponseCoinDto responseCoinDto = new ResponseCoinDto();
    BeanUtils.copyProperties(coinItem, responseCoinDto);
    if (coinDetailsDto != null) {
      responseCoinDto.setDataFresh(true);
      try {
        responseCoinDto.setTradeUrl(coinDetailsDto.getTickers().get(0).getTradeUrl());
        responseCoinDto.setDescription(coinDetailsDto.getDescription().getEn());
      } catch (NullPointerException e) {
        //do nothing
      }
    }

    return responseCoinDto;
  }

  @Transactional
  void cacheCoinDataIntoDb(List<ResponseCoinDto> responseCoinDtos, VsCurrency vsCurrency) {

    List<Coin> coinListToDb = new ArrayList<>();
    List<Price> priceListToDb = new ArrayList<>();
    for (ResponseCoinDto coinDto : responseCoinDtos) {
      if (!coinDto.isDataFresh()) {
        continue;
      }

      Coin newCoin = new Coin();
      BeanUtils.copyProperties(coinDto, newCoin);
      newCoin.setLastUpdatedTime(Instant.now());

      Price price = new Price();
      price.setPricePK(new PricePK(coinDto.getId(), vsCurrency.getCurrency()));
      price.setCoin(newCoin);
      price.setCurrency(vsCurrency);
      price.setCurrentPrice(coinDto.getCurrentPrice());
      priceListToDb.add(price);

      coinListToDb.add(newCoin);
    }
    priceRepository.saveAll(priceListToDb);
    coinRepository.saveAll(coinListToDb);
    log.info("Saved {} coins into db", coinListToDb.size());
  }
}
