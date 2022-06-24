package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.dto.app.ResponseCoinDto;
import com.gable.currencyapp.dto.gecko.CoinDetailsDto;
import com.gable.currencyapp.dto.gecko.CoinListByCurrency;
import com.gable.currencyapp.dto.gecko.SimpleCoinDto;
import com.gable.currencyapp.model.Coin;
import com.gable.currencyapp.repository.CoinRepository;
import com.gable.currencyapp.service.CoinService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

  private final CoinRepository coinRepository;
  private final WebClient webClient;

  @Override
  public void retrieveCoinList() {

    if (coinRepository.count() > 0) {
      log.info("Coin list already has data");
      return;
    }

    //this should run once
    log.info("Querying into coingecko to get coin list");
    Mono<SimpleCoinDto[]> coinArrMono = webClient.get()
        .uri("/coins/list")
        .retrieve()
        .bodyToMono(SimpleCoinDto[].class);

    SimpleCoinDto[] coinArray = coinArrMono.block();
    if (coinArray == null) {
      log.error("Unable to get coin from coingecko");
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
    log.info("Get list of coins by currency");
    CoinListByCurrency[] coinList = webClient.get()
        .uri(
            uriBuilder -> uriBuilder.path("/coins/markets/")
                .queryParam("vs_currency", currency)
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .build()
        )
        .exchangeToMono(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(CoinListByCurrency[].class);
              } else {
                log.error("Unable to get data for currency {}. Possibly limit reached", currency);
                return Mono.empty();
              }
            }
        )
        .block();

    if (coinList == null) {
      return new ArrayList<>();
    }
    log.info("coinlist {}", coinList.length);

    List<ResponseCoinDto> responseCoinDtos = new ArrayList<>();
    //call server to get coin data
    // if 429 then get data from db with lastUpdatedTime < 1hr or return blank
    for (CoinListByCurrency coinItem : coinList) {
      log.info("coin item query data {}", coinItem.toString());
      CoinDetailsDto coinDetailsDto = webClient.get()
          .uri("/coins/" + coinItem.getId())
          .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                  return response.bodyToMono(CoinDetailsDto.class);
                } else {
                  log.error("Unable to get data for coin id {}. Possibly limit reached", coinItem.getId());
                  return Mono.empty();
                }
              }
          )
          .block();
      ResponseCoinDto responseCoinDto = new ResponseCoinDto();
      BeanUtils.copyProperties(coinItem, responseCoinDto);
      if (coinDetailsDto != null) {
        log.info("response {}", coinDetailsDto.toString());
        try {
          responseCoinDto.setTradeUrl(coinDetailsDto.getTickers().get(0).getTradeUrl());
          responseCoinDto.setDescription(coinDetailsDto.getDescription().getEn());
        } catch (NullPointerException e) {
          //do nothing
        }
      }

      responseCoinDtos.add(responseCoinDto);
    }

    return responseCoinDtos;
  }
}
