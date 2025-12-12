package com.matejik.terminal.application.command;

public interface CallControlPort {

  void startOutboundCall(String destination);

  void hangUp(String callId);

  void answer(String callId);
}
