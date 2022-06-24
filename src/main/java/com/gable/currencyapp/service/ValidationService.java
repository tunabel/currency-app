package com.gable.currencyapp.service;

import com.gable.currencyapp.dto.app.RequestCoinDto;
import com.gable.currencyapp.exception.InvalidRequestException;

public interface ValidationService {

  void isRequestDtoValid(RequestCoinDto requestCoinDto) throws InvalidRequestException;

}
