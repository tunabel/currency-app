package com.gable.currencyapp.service;

import com.gable.currencyapp.dto.app.ResponseCoinDto;
import com.gable.currencyapp.model.VsCurrency;
import java.util.List;

public interface CoinService {

  void retrieveCoinList();

  List<ResponseCoinDto> getCoinResponseDtosByCurrency(VsCurrency currency, int page, int perPage);
}
