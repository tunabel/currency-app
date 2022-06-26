package com.gable.currencyapp.repository.projection;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;

public interface CoinWithPrice {

  @Value("#{target.coin_id}")
  String getId();

  @Value("#{target.coin_name}")
  String getName();

  @Value("#{target.coin_symbol}")
  String getSymbol();

  @Value("#{target.coin_image}")
  String getImage();

  @Value("#{target.coin_description}")
  String getDescription();

  @Value("#{target.price_change_percentage_24h}")
  BigDecimal getPriceChangePercent();

  @Value("#{target.market_cap_rank}")
  int getMarketCapRank();

  @Value("#{target.trade_url}")
  String getTradeUrl();

  @Value("#{target.current_price}")
  BigDecimal getCurrentPrice();
}
