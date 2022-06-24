package com.gable.currencyapp.repository;

import com.gable.currencyapp.model.VsCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VsCurrencyRepository extends JpaRepository<VsCurrency, String> {

}