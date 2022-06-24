package com.gable.currencyapp.dto.gecko;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerDto {

  @JsonProperty("trade_url")
  private String tradeUrl;

}
