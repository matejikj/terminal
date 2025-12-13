package com.matejik.skoda;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.matejik.skoda", "com.matejik.terminal"})
@Theme(value = "terminal")
public class SkodaApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(SkodaApplication.class, args);
  }
}
