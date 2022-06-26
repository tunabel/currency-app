package com.gable.currencyapp.dto.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gable.currencyapp.util.TwoDecimalSerializer;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCoinDto {

  private static final String UPDATING_DATA = "Updating data...";
  @JsonIgnore
  private String id;

  @JsonIgnore
  private boolean isDataFresh = false;

  @JsonIgnore
  private int marketCapRank;

  private String image;

  private String symbol;

  private String name;

  @JsonProperty("price_change_percentage_24h")
  @JsonSerialize(using = TwoDecimalSerializer.class)
  private BigDecimal priceChangePercent;

  @JsonProperty("current_price")
  @JsonSerialize(using = TwoDecimalSerializer.class)
  private BigDecimal currentPrice;

  private String description = UPDATING_DATA;

  @JsonProperty("trade_url")
  private String tradeUrl = UPDATING_DATA;
}
