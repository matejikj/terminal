package com.matejik.terminal.application.system;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DesktopBrowserLauncher {

  private static final Logger LOGGER = LoggerFactory.getLogger(DesktopBrowserLauncher.class);

  private final boolean autoLaunch;
  private final String launchUrlTemplate;
  private final Duration launchDelay;
  private final ServletWebServerApplicationContext serverApplicationContext;
  private final AtomicBoolean launched = new AtomicBoolean(false);

  public DesktopBrowserLauncher(
      @Value("${terminal.desktop.auto-browser:true}") boolean autoLaunch,
      @Value("${terminal.desktop.launch-url:http://localhost:{port}}") String launchUrlTemplate,
      @Value("${terminal.desktop.browser-delay:PT1S}") Duration launchDelay,
      ServletWebServerApplicationContext serverApplicationContext) {
    this.autoLaunch = autoLaunch;
    this.launchUrlTemplate =
        StringUtils.hasText(launchUrlTemplate) ? launchUrlTemplate : "http://localhost:{port}";
    this.launchDelay = launchDelay == null ? Duration.ofSeconds(1) : launchDelay;
    this.serverApplicationContext = serverApplicationContext;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    if (!autoLaunch) {
      LOGGER.debug("Desktop auto-launch disabled.");
      return;
    }
    if (!launched.compareAndSet(false, true)) {
      return;
    }
    if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
      LOGGER.info("Desktop browsing is not supported in this environment.");
      return;
    }
    var desktop = Desktop.getDesktop();
    if (!desktop.isSupported(Desktop.Action.BROWSE)) {
      LOGGER.info("The Desktop API does not support browsing on this platform.");
      return;
    }

    var targetUrl = resolveLaunchUrl();
    if (!StringUtils.hasText(targetUrl)) {
      LOGGER.warn("Unable to resolve launch URL.");
      return;
    }

    CompletableFuture.runAsync(
        () -> {
          try {
            if (!launchDelay.isZero() && !launchDelay.isNegative()) {
              Thread.sleep(launchDelay.toMillis());
            }
            desktop.browse(new URI(targetUrl));
            LOGGER.info("Opened default browser at {}", targetUrl);
          } catch (Exception exception) {
            LOGGER.warn("Failed to open browser automatically.", exception);
          }
        });
  }

  private String resolveLaunchUrl() {
    var webServer = serverApplicationContext.getWebServer();
    if (webServer == null) {
      LOGGER.warn("Web server not initialized yet, cannot resolve launch URL.");
      return null;
    }
    var port = webServer.getPort();
    var template = launchUrlTemplate;
    if (template.contains("{port}")) {
      return template.replace("{port}", Integer.toString(port));
    }
    return template;
  }
}
