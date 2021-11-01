package com.closememo.gateway.infra.http.query;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties("http.query")
@Configuration
public class QueryProperties {

  private String baseUrl;
  private int readTimeout;
  private int connectionTimeout;
  private int maxConnectionCount;
  private int maxConnectionPerRoute;
}
