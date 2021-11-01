package com.closememo.gateway.infra.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountId {

  private String id;

  public AccountId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }
}
