package com.matejik.product;

import com.matejik.Application;
import org.springframework.boot.SpringApplication;

public final class ProductApplication {

    private ProductApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("product");
        app.run(args);
    }
}
