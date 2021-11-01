package com.closememo.gateway.config.filter;

import lombok.Getter;

@Getter
public enum TokenHeaders {
  ACCESS_TOKEN("X-ACCESS-TOKEN", "로그인 토큰"),
  SYNC_TOKEN("X-SYNC-TOKEN", "동기화 토큰"),
  ;

  private final String headerName;
  private final String description;

  TokenHeaders(String headerName, String description) {
    this.headerName = headerName;
    this.description = description;
  }
}
