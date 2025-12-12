package com.matejik.terminal.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppStoreConfiguration {

  @Bean(name = "sharedBackendExecutor", destroyMethod = "shutdown")
  ExecutorService sharedBackendExecutor() {
    var threads = Math.max(4, Runtime.getRuntime().availableProcessors());
    return Executors.newFixedThreadPool(threads, new NamedThreadFactory("app-store-dispatch"));
  }

  private static final class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger counter = new AtomicInteger(1);

    private NamedThreadFactory(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
      var thread = new Thread(runnable);
      thread.setDaemon(true);
      thread.setName(prefix + '-' + counter.getAndIncrement());
      return thread;
    }
  }
}
