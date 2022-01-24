package com.closememo.gateway.config.filter;

import com.closememo.gateway.config.cache.EhCacheConfig;
import com.closememo.gateway.infra.model.Account;
import com.closememo.gateway.infra.model.AccountId;
import com.closememo.gateway.infra.model.BusinessException;
import com.closememo.gateway.config.filter.AuthenticationGatewayFilterFactory.Config;
import com.closememo.gateway.infra.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

@Slf4j
@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

  private static final String X_BYPASS_ACCOUNT_ID = "X-Bypass-Account-Id";
  private static final String X_ACCOUNT_ID_HEADER_NAME = "X-Account-Id";
  private static final String X_ACCOUNT_ROLE_HEADER_NAME = "X-Account-Roles";

  private final AccountService accountService;
  private final CacheManager ehCacheManager;
  private final String activeProfiles;

  public AuthenticationGatewayFilterFactory(
      AccountService accountService,
      @Qualifier("ehCacheManager") CacheManager ehCacheManager,
      @Value("${spring.profiles.active}") String activeProfiles) {
    super(Config.class);
    this.accountService = accountService;
    this.ehCacheManager = ehCacheManager;
    this.activeProfiles = activeProfiles;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> getAccount(exchange.getRequest())
        .map(account -> exchange.mutate()
            .request(builder -> builder
                .header(X_ACCOUNT_ID_HEADER_NAME, account.getId().getId())
                .header(X_ACCOUNT_ROLE_HEADER_NAME, account.getRoles().toArray(new String[]{}))
            )
            .build())
        .switchIfEmpty(Mono.just(
            exchange.mutate()
                .request(builder -> builder.headers(httpHeaders -> {
                  httpHeaders.remove(X_ACCOUNT_ID_HEADER_NAME);
                  httpHeaders.remove(X_ACCOUNT_ROLE_HEADER_NAME);
                }))
                .build()))
        .flatMap(chain::filter)
        .doOnError(throwable -> {
          if (!(throwable instanceof BusinessException)
              || ((BusinessException) throwable).isNecessaryToLog()) {
            log.error(throwable.getMessage(), throwable);
          }
        });
  }

  private Mono<Account> getAccount(@NonNull ServerHttpRequest request) {
    String bypassAccountId = getBypassAccountId(request);
    if (StringUtils.isNotBlank(bypassAccountId)) {
      return accountService.getAccountById(new AccountId(bypassAccountId));
    }

    String accessToken = request.getHeaders().getFirst(TokenHeaders.ACCESS_TOKEN.getHeaderName());

    if (StringUtils.isBlank(accessToken)) {
      return Mono.empty();
    }

    Cache<String, Account> cache = ehCacheManager
        .getCache(EhCacheConfig.ACCOUNT_CACHE_NAME, String.class, Account.class);

    return CacheMono
        .lookup(key -> Mono.justOrEmpty(cache.get(key)).map(Signal::next), accessToken)
        .onCacheMissResume(() -> accountService.getAccountByToken(accessToken))
        .andWriteWith((key, signal) ->
            Mono.fromRunnable(() -> {
              if (!signal.isOnError() && signal.get() != null) {
                cache.put(accessToken, signal.get());
              }
            }));
  }

  private String getBypassAccountId(@NonNull ServerHttpRequest request) {
    return activeProfiles.contains("local") || activeProfiles.contains("dev")
        ? request.getHeaders().getFirst(X_BYPASS_ACCOUNT_ID)
        : null;
  }

  public static class Config {
    // Empty
  }
}
