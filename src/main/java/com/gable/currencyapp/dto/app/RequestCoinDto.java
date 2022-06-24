package com.gable.currencyapp.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCoinDto {

  @NotBlank
  private String currency;

  @Min(value = 0)
  private int page = 0;

  @Min(1)
  @Max(250)
  @JsonProperty("per_page")
  private int perPage = 10;

}
