package com.gable.currencyapp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vs_currency")
@Getter
@Setter
public class VsCurrency {

  @Id
  @Column(name = "currency")
  private String currency;

}
