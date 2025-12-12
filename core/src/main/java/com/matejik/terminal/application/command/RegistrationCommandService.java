package com.matejik.terminal.application.command;

import com.matejik.sip.SipAccountCredentials;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class RegistrationCommandService {

  private final RegistrationControlPort controlPort;

  public RegistrationCommandService(RegistrationControlPort controlPort) {
    this.controlPort = controlPort;
  }

  public void connect(SipAccountCredentials credentials) {
    controlPort.connect(credentials);
  }

  public void disconnect() {
    controlPort.disconnect();
  }
}
