package com.gable.currencyapp.config;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

@Configuration
public class TaskSchedulerConfig {

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(1);
    threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    return threadPoolTaskScheduler;
  }

  @Bean("startingPeriodicTrigger")
  public PeriodicTrigger startingPeriodicTrigger() {
    PeriodicTrigger periodicTrigger = new PeriodicTrigger(3000, TimeUnit.MILLISECONDS);
    periodicTrigger.setFixedRate(true);
    periodicTrigger.setInitialDelay(5000);
    return periodicTrigger;
  }

  @Bean("restartedPeriodicTrigger")
  public PeriodicTrigger restartedPeriodicTrigger() {
    PeriodicTrigger periodicTrigger = new PeriodicTrigger(3000, TimeUnit.MILLISECONDS);
    periodicTrigger.setFixedRate(true);
    periodicTrigger.setInitialDelay(0);
    return periodicTrigger;
  }

}
