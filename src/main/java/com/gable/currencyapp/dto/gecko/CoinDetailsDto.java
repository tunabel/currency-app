package com.gable.currencyapp.dto.gecko;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CoinDetailsDto {

  @JsonProperty("id")
  private String coinId;

  private String name;

  private String symbol;

  @JsonProperty("price_change_percentage_24h")
  private BigDecimal priceChangePercent;

  @JsonProperty("current_price")
  private BigDecimal currentPrice;

  @JsonProperty("description")
  private DescriptionDto description;

  @JsonProperty("tickers")
  private List<TickerDto> tickers;

}
