package com.gable.currencyapp.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "coin", indexes = {
    @Index(name = "idx_coin_id", columnList = "coin_id")
})
@Getter
@Setter
public class Coin {

  @Id
  @Column(name = "coin_id")
  private String id;

  @Column(name = "coin_symbol")
  private String symbol;

  @Column(name = "coin_image", columnDefinition = "TEXT")
  private String image;

  @Column(name = "coin_name")
  private String name;

  @Column(name = "price_change_percentage_24h")
  private BigDecimal priceChangePercent;

  @Column(name = "coin_description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "trade_url")
  private String tradeUrl;

  @Column(name = "last_updated_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant lastUpdatedTime;

  @Column(name = "market_cap_rank")
  private int marketCapRank;

  @Cascade(value = CascadeType.PERSIST)
  @OneToMany(mappedBy = "coin")
  Set<Price> prices = new HashSet<>();
}
