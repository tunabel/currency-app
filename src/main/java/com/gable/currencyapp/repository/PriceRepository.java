package com.gable.currencyapp.repository;

import com.gable.currencyapp.model.Price;
import com.gable.currencyapp.model.PricePK;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, PricePK> {

  Optional<Price> findPriceByPricePK(PricePK pricePK);

}