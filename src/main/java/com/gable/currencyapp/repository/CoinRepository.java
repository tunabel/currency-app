package com.gable.currencyapp.repository;

import com.gable.currencyapp.model.Coin;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends JpaRepository<Coin, String> {

  @Query(value = "select * from coin where coin.coin_id in (:ids) and coin.last_updated_time > (now() - (interval '1' hour) * :lastUpdatedPeriod)", nativeQuery = true)
  List<Coin> findCoinsByIdAndLastUpdatedPeriod(
      @Param("ids") List<String> ids,
      @Param("lastUpdatedPeriod") double lastUpdatedPeriod
  );
}