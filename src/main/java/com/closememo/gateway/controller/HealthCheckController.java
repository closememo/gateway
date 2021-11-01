package com.closememo.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  private static final String OK = "OK";

  @GetMapping("/health-check")
  public String check() {
    return OK;
  }
}
