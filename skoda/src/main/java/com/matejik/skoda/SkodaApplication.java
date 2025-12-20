package com.matejik.skoda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.matejik.skoda", "com.matejik.terminal"})
public class SkodaApplication {

  public static void main(String[] args) {
    SpringApplication.run(SkodaApplication.class, args);
  }
}
