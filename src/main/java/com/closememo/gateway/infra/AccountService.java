package com.closememo.gateway.infra;

import com.closememo.gateway.infra.http.query.QueryClient;
import com.closememo.gateway.infra.model.Account;
import com.closememo.gateway.infra.model.AccountId;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountService {

  private final QueryClient queryClient;

  public AccountService(QueryClient queryClient) {
    this.queryClient = queryClient;
  }

  public Mono<Account> getAccountById(AccountId accountId) {
    if (accountId == null) {
      return Mono.empty();
    }

    return queryClient.getAccountById(accountId);
  }

  public Mono<Account> getAccountByToken(String accessToken, String syncToken) {
    if (StringUtils.isEmpty(accessToken)) {
      return Mono.empty();
    }

    return queryClient.getAccountByToken(accessToken, syncToken);
  }
}
