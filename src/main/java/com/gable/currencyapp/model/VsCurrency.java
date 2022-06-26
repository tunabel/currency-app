package com.gable.currencyapp.model;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vs_currency")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VsCurrency {

  @Id
  @Column(name = "currency")
  private String currency;

  @OneToMany(mappedBy = "currency")
  Set<Price> prices;

  @Column(name = "crawl_priority")
  private byte crawlPriority = 0;

}
