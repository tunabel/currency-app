package com.gable.currencyapp.service;

import com.gable.currencyapp.model.VsCurrency;

public interface VsCurrencyService {

  void retrieveCurrencies();

  VsCurrency updateVsCurrencyPriority(String currency);
}
