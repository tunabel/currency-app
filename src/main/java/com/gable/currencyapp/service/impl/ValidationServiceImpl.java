package com.gable.currencyapp.service.impl;

import com.gable.currencyapp.dto.app.RequestCoinDto;
import com.gable.currencyapp.exception.InvalidRequestException;
import com.gable.currencyapp.repository.VsCurrencyRepository;
import com.gable.currencyapp.service.ValidationService;
import java.util.Locale;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Log4j2
public class ValidationServiceImpl implements ValidationService {

  private final VsCurrencyRepository vsCurrencyRepository;

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Override
  public void isRequestDtoValid(@Validated RequestCoinDto requestCoinDto)
      throws InvalidRequestException {

    Set<ConstraintViolation<RequestCoinDto>> constraints = validator.validate(requestCoinDto);

    if (!constraints.isEmpty()) {
      throw new InvalidRequestException(
          constraints.stream()
              .reduce("",
                  (error, constraint) -> error + constraint.getPropertyPath() + " "
                      + constraint.getMessage() + "; ",
                  String::concat)
      );
    }

    if (!vsCurrencyRepository.existsById(requestCoinDto.getCurrency().toLowerCase(Locale.ROOT))) {
      throw new InvalidRequestException("Currency doesn't exist");
    }
  }
}
