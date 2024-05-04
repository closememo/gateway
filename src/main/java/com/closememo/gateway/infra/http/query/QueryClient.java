package com.closememo.gateway.infra.http.query;

import com.closememo.gateway.infra.model.Account;
import com.closememo.gateway.infra.model.AccountId;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
public class QueryClient {

  private final WebClient webClient;

  public QueryClient(@Value("${secret.system-key}") String systemKey, QueryProperties properties) {

    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectionTimeout())
        .doOnConnected(connection ->
            connection.addHandlerLast(
                new ReadTimeoutHandler(properties.getReadTimeout(), TimeUnit.MILLISECONDS)));

    this.webClient = WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("X-SYSTEM-KEY", systemKey)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter((request, next) -> {
          log.debug("HTTP Method : {}", request.method());
          log.debug("URL : {}", request.url());
          request.headers().forEach((name, values) ->
              values.forEach((value) ->
                  log.debug("HEADER : {}={}", name, value)));
          return next.exchange(request);
        })
        .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
          HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
          if (status.isError()) {
            if (status.is5xxServerError()) {
              log.error(String
                  .format("[FAIL] HTTP Status:%s(%d)", status.name(), status.value()));
            } else {
              log.warn(String
                  .format("[FAIL] HTTP Status:%s(%d)", status.name(), status.value()));
            }
          }
          return Mono.just(clientResponse);
        }))
        .build();
  }

  public Mono<Account> getAccountById(AccountId accountId) {
    return webClient
        .get()
        .uri("/query/system/accounts/{id}", accountId.getId())
        .retrieve()
        .bodyToMono(Account.class)
        .onErrorResume(throwable -> {
          log.error(throwable.getMessage(), throwable);
          return Mono.empty();
        });
  }

  public Mono<Account> getAccountByToken(String accessToken) {
    return webClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path("/query/system/account-by-token")
            .queryParam("accessToken", accessToken)
            .build())
        .retrieve()
        .bodyToMono(Account.class)
        .onErrorResume(throwable -> {
          log.error(throwable.getMessage(), throwable);
          return Mono.empty();
        });
  }
}
