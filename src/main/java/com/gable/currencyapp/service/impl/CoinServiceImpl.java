package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.dto.app.ResponseCoinDto;
import com.gable.currencyapp.dto.gecko.CoinDetailsDto;
import com.gable.currencyapp.dto.gecko.CoinByCurrency;
import com.gable.currencyapp.dto.gecko.SimpleCoinDto;
import com.gable.currencyapp.model.Coin;
import com.gable.currencyapp.repository.CoinRepository;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

  private final CoinRepository coinRepository;
  private final WebClient webClient;

  @Value("${currency-app.coin-gecko.path.simple-coin-list}")
  private String simpleCoinListPath;

  @Value("${currency-app.coin-gecko.path.coin-list}")
  private String coinListPath;

  @Value("${currency-app.coin-gecko.path.coin-details}")
  private String coinDetailsPath;

  @Value("${currency-app.last-updated-period}")
  private double lastUpdatedPeriod;

  @Override
  public void retrieveCoinList() {
    if (coinRepository.count() > 0) {
      log.info("Coin list already has data. Won't query data again");
      return;
    }

    log.info("Querying into coin-gecko to get simple coin list");
    SimpleCoinDto[] coinArray = webClient.get()
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
  public List<ResponseCoinDto> getCoinsByCurrency(String currency, int page, int perPage) {

    log.info("Get list of coins by currency {}", currency);
    CoinByCurrency[] coinList = queryCoinListByCurrencies(currency, page, perPage);

    if (coinList == null) {
      log.info("Unable to get coin list by currency {}", currency);
      return new ArrayList<>();
    }

    List<ResponseCoinDto> responseCoinDtos = createResponseCoinDtos(coinList);

    cacheCoinDataIntoDb(responseCoinDtos);

    return responseCoinDtos;
  }

  public List<ResponseCoinDto> createResponseCoinDtos(CoinByCurrency[] coinList) {
    List<String> coinIds = Arrays.stream(coinList)
        .map(CoinByCurrency::getId)
        .collect(Collectors.toList());

    List<Coin> lastHourCoinDataList = coinRepository
        .findCoinsByIdAndLastUpdatedPeriod(coinIds, lastUpdatedPeriod);

    List<ResponseCoinDto> responseCoinDtos = new ArrayList<>();

    for (CoinByCurrency coinByCurrency : coinList) {
      ResponseCoinDto responseCoinDto;
      Optional<Coin> matchCoinInDbOptional = lastHourCoinDataList
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

  private ResponseCoinDto createResponseCoinDtoFromDbCoin(Coin coin) {
    log.info("Map data from database to response for coin {}", coin.getId());
    ResponseCoinDto responseCoinDto = new ResponseCoinDto();
    BeanUtils.copyProperties(coin, responseCoinDto);
    return responseCoinDto;
  }

  private ResponseCoinDto createResponseCoinDtoFromServer(CoinByCurrency coinItem) {
    log.info("Query data of coin {}", coinItem.getId());
    CoinDetailsDto coinDetailsDto = webClient.get()
        .uri(coinDetailsPath + coinItem.getId())
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(CoinDetailsDto.class);
              } else {
                log.error("Unable to get data for coin id {}. Possibly limit reached",
                    coinItem.getId());
                return Mono.empty();
              }
            }
        )
        .block();

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

  private void cacheCoinDataIntoDb(List<ResponseCoinDto> responseCoinDtos) {
    List<Coin> coinListToDb = responseCoinDtos.stream()
        .filter(ResponseCoinDto::isDataFresh)
        .map(coinDto -> {
          Coin newCoin = new Coin();
          BeanUtils.copyProperties(coinDto, newCoin);
          newCoin.setLastUpdatedTime(Instant.now());
          return newCoin;
        })
        .collect(Collectors.toList());
    coinRepository.saveAll(coinListToDb);
    log.info("Saved {} coins into db", coinListToDb.size());
  }

  private CoinByCurrency[] queryCoinListByCurrencies(String currency, int page,
      int perPage) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder.path(coinListPath)
            .queryParam("vs_currency", currency)
            .queryParam("page", page)
            .queryParam("per_page", perPage)
            .build()
        )
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(CoinByCurrency[].class);
              } else {
                log.error("Unable to get data for currency {}. Possibly limit reached", currency);
                return Mono.empty();
              }
            }
        )
        .block();
  }
}
