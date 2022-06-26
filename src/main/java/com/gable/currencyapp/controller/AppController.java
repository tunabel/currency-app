package com.gable.currencyapp.controller;

import com.gable.currencyapp.dto.app.RequestCoinDto;
import com.gable.currencyapp.dto.app.ResponseCoinDto;
import com.gable.currencyapp.exception.InvalidRequestException;
import com.gable.currencyapp.model.VsCurrency;
import com.gable.currencyapp.service.CoinService;
import com.gable.currencyapp.service.ValidationService;
import com.gable.currencyapp.service.VsCurrencyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coins")
@RequiredArgsConstructor
public class AppController {

  private final ValidationService validationService;
  private final CoinService coinService;
  private final VsCurrencyService vsCurrencyService;

  @GetMapping("/get_coins")
  public ResponseEntity<List<ResponseCoinDto>> getCoinsByCurrency(
      @RequestBody RequestCoinDto requestDto
  ) throws InvalidRequestException {

    validationService.isRequestDtoValid(requestDto);

//      prioritize crawling data for queried currency
    VsCurrency vsCurrency = vsCurrencyService.updateVsCurrencyPriority(requestDto.getCurrency());

    //page starts from 0, whilst coinGecko server accepts 1 as min value for page
    List<ResponseCoinDto> coinResponse = coinService.getCoinResponseDtosByCurrency(
        vsCurrency,
        requestDto.getPage() + 1,
        requestDto.getPerPage()
    );
    return ResponseEntity.ok(coinResponse);
  }
}
