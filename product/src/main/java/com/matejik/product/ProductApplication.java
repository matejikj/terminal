package com.matejik.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.matejik.product", "com.matejik.terminal"})
public class ProductApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProductApplication.class, args);
  }
}
