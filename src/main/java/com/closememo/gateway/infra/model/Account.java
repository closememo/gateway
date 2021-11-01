package com.closememo.gateway.infra.model;

import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

  private AccountId id;
  private Set<String> roles;
}
