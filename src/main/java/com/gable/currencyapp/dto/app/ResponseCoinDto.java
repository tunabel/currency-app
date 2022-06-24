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

  @JsonIgnore
  private String id;

  @JsonIgnore
  private boolean isDataFresh = false;

  @JsonIgnore
  private long marketCapRank;

  private String image;

  private String symbol;

  private String name;

  @JsonProperty("price_change_percentage_24h")
  @JsonSerialize(using = TwoDecimalSerializer.class)
  private BigDecimal priceChangePercent;

  @JsonProperty("current_price")
  @JsonSerialize(using = TwoDecimalSerializer.class)
  private BigDecimal currentPrice;

  private String description;

  @JsonProperty("trade_url")
  private String tradeUrl;
}
