package com.matejik.product;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.matejik.product", "com.matejik.terminal"})
@Theme(value = "terminal")
public class ProductApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(ProductApplication.class, args);
  }
}
