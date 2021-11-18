package com.closememo.gateway.config.cache;

import com.closememo.gateway.infra.model.Account;
import java.time.Duration;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EhCacheConfig {

  public final static String ACCOUNT_CACHE_NAME = "account";

  @Bean
  public CacheManager ehCacheManager() {
    ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder
        .newResourcePoolsBuilder()
        .heap(10000L, EntryUnit.ENTRIES);

    CacheConfigurationBuilder<String, Account> cacheConfigurationBuilder = CacheConfigurationBuilder
        .newCacheConfigurationBuilder(String.class, Account.class, resourcePoolsBuilder)
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(30)));

    return CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(ACCOUNT_CACHE_NAME, cacheConfigurationBuilder)
        .build(true);
  }
}
