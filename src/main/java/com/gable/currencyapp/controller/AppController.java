package com.gable.currencyapp.controller;

import com.gable.currencyapp.dto.app.RequestCoinDto;
import com.gable.currencyapp.dto.app.ResponseCoinDto;
import com.gable.currencyapp.exception.InvalidRequestException;
import com.gable.currencyapp.service.ValidationService;
import java.util.ArrayList;
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

  @GetMapping("/get_coins")
  public ResponseEntity<List<ResponseCoinDto>> getCoinsByCurrency(
      @RequestBody RequestCoinDto requestDto
  ) throws InvalidRequestException {
      validationService.isRequestDtoValid(requestDto);
      return ResponseEntity.ok(new ArrayList<>());
  }
}
