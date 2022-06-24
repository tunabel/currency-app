package com.gable.currencyapp.dto.gecko;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CoinListByCurrency {

  private String id;

  private String symbol;

  private String name;

  private String image;

  @JsonProperty("price_change_percentage_24h")
  private BigDecimal priceChangePercent;

  @JsonProperty("current_price")
  private BigDecimal currentPrice;
}
