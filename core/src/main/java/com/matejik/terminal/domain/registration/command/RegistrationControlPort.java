package com.matejik.terminal.domain.registration.command;

import com.matejik.sip.SipAccountCredentials;

public interface RegistrationControlPort {

  void connect(SipAccountCredentials credentials);

  void disconnect();
}
