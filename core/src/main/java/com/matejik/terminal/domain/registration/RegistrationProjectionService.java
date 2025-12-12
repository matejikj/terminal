package com.matejik.terminal.domain.registration;

import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.registration.event.RegistrationDomainEvent;
import com.matejik.terminal.domain.registration.state.RegistrationAction;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class RegistrationProjectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationProjectionService.class);

  private final AppStore appStore;

  public RegistrationProjectionService(AppStore appStore) {
    this.appStore = appStore;
  }

  @EventListener
  public void onConnectionChanged(RegistrationDomainEvent.ConnectionChanged event) {
    appStore
        .dispatch(
            new RegistrationAction.UpdateStatus(
                event.status(), event.accountLabel(), event.message(), Instant.now()))
        .exceptionally(
            error -> {
              LOGGER.error("Failed to project registration update", error);
              return null;
            });
  }
}
