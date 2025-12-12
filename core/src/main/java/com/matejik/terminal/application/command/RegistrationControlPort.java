package com.matejik.terminal.application.command;

import com.matejik.sip.SipAccountCredentials;

public interface RegistrationControlPort {

  void connect(SipAccountCredentials credentials);

  void disconnect();
}
