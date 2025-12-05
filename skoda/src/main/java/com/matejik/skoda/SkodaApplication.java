package com.matejik.skoda;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkodaApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SkodaApplication.class, args);
    }
}
