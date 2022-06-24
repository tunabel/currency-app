package com.gable.currencyapp.service;

import com.gable.currencyapp.dto.app.ResponseCoinDto;
import java.util.List;

public interface CoinService {

  void retrieveCoinList();

  List<ResponseCoinDto> getCoinsByCurrency(String currency, int page, int perPage);
}
