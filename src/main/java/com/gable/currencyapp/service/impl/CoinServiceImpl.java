package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.dto.gecko.SimpleCoinDto;
import com.gable.currencyapp.model.Coin;
import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.repository.CoinRepository;
import com.gable.currencyapp.service.CoinService;
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
}
