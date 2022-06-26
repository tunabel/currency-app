package com.gable.currencyapp.repository;

import com.gable.currencyapp.model.Price;
import com.gable.currencyapp.model.PricePK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, PricePK> {

}