package com.matejik.terminal.application.system;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationShutdownService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationShutdownService.class);

  private final ConfigurableApplicationContext applicationContext;
  private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

  public ApplicationShutdownService(ConfigurableApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void requestShutdown() {
    if (!shuttingDown.compareAndSet(false, true)) {
      LOGGER.info("Shutdown already requested, skipping duplicate invocation.");
      return;
    }
    LOGGER.info("User requested application shutdown – closing Spring context.");
    CompletableFuture.runAsync(
        () -> {
          try {
            int exitCode = SpringApplication.exit(applicationContext, () -> 0);
            LOGGER.info("Application context closed, exiting JVM with code {}", exitCode);
            System.exit(exitCode);
          } catch (Exception exception) {
            LOGGER.error("Failed to shut down gracefully, forcing JVM exit.", exception);
            System.exit(1);
          }
        });
  }

  public boolean isShuttingDown() {
    return shuttingDown.get();
  }
}
