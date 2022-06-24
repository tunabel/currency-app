package com.gable.currencyapp.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCoinDto {

  private String image;
  private String symbol;
  private String name;
  @JsonProperty("price_change_percentage_24h")
  private String priceChangePercent;
  @JsonProperty("current_price")
  private String currentPrice;
  private String description;
  @JsonProperty("trade_url")
  private String tradeUrl;

}
