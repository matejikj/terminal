package com.matejik.skoda;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ServletInitializer for WAR deployment to application servers (Wildfly, Tomcat, etc.)
 * This class is only used when packaging the application as WAR.
 */
public class ServletInitializer extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(SkodaApplication.class);
  }
}
