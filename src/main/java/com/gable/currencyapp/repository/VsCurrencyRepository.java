package com.gable.currencyapp.repository;

import com.gable.currencyapp.model.VsCurrency;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VsCurrencyRepository extends JpaRepository<VsCurrency, String> {

  @Query("select c from VsCurrency c order by c.crawlPriority desc")
  List<VsCurrency> findAllOrderByPriority();

  Optional<VsCurrency> findByCrawlPriority(byte priority);

  @Query(value = "select * from vs_currency c where c.crawl_priority is not null order by c.crawl_priority", nativeQuery = true)
  List<VsCurrency> findAllWithPriority();

}