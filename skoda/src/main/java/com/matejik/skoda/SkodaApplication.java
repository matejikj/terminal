package com.matejik.skoda;

import com.matejik.Application;
import org.springframework.boot.SpringApplication;

public final class SkodaApplication {

    private SkodaApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("skoda");
        app.run(args);
    }
}
