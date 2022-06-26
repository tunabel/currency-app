package com.gable.currencyapp.model;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "coin_in_currency")
@Getter
@Setter
public class Price {

  @EmbeddedId
  private PricePK pricePK = new PricePK();

  @ManyToOne
  @MapsId("coin_id")
  @JoinColumn(name = "coin_id")
  private Coin coin;

  @ManyToOne
  @MapsId("currency")
  @JoinColumn(name = "currency")
  private VsCurrency currency;

  @Column(name = "current_price", scale = 20)
  private BigDecimal currentPrice;


}
