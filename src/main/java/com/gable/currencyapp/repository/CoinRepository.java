package com.gable.currencyapp.repository;

import com.gable.currencyapp.model.Coin;
import com.gable.currencyapp.repository.projection.CoinWithPrice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends JpaRepository<Coin, String> {

  @Query(value = "select * from coin where coin.coin_id in (:ids) and coin.last_updated_time >= (now() - (interval '1' hour) * :lastUpdatedPeriod)", nativeQuery = true)
  List<Coin> findCoinsByIdAndLastUpdatedPeriod(
      @Param("ids") List<String> ids,
      @Param("lastUpdatedPeriod") double lastUpdatedPeriod
  );

  @Query(value = "select coin.*, cic.current_price from coin join coin_in_currency cic on coin.coin_id = cic.coin_id where coin.coin_id in (:ids) and cic.currency = :currency and coin.last_updated_time > (now() - (interval '1' hour) * :lastUpdatedPeriod)", nativeQuery = true)
  List<CoinWithPrice> findCoinDataByIdAndCurrencyAndLastUpdatedPeriod(
      @Param("ids") List<String> ids,
      @Param("currency") String currency,
      @Param("lastUpdatedPeriod") double lastUpdatedPeriod
  );

  @Query(value = "select * from coin where coin.coin_id in (:ids) and coin.last_updated_time < (now() - (interval '1' hour) * :lastUpdatedPeriod)", nativeQuery = true)
  List<Coin> findCoinsByIdAndNotUpdated(
      @Param("ids") List<String> ids,
      @Param("lastUpdatedPeriod") double lastUpdatedPeriod
  );

  @Query(value = "select * from coin where coin.coin_id = :id and coin.last_updated_time > (now() - (interval '1' hour) * :lastUpdatedPeriod)", nativeQuery = true)
  Optional<Coin> findCoinByIdAndLastUpdatedPeriod(
      @Param("id") String ids,
      @Param("lastUpdatedPeriod") double lastUpdatedPeriod
  );


}