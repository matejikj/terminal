package com.matejik.product;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(ProductApplication.class, args);
  }
}
