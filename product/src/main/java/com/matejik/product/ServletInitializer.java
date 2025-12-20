package com.matejik.product;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ServletInitializer for WAR deployment to application servers like Wildfly.
 * This class extends SpringBootServletInitializer to enable the application
 * to be deployed as a traditional WAR file.
 */
public class ServletInitializer extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(ProductApplication.class);
  }
}
