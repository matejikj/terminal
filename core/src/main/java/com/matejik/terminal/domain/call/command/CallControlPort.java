package com.matejik.terminal.domain.call.command;

public interface CallControlPort {

  void startOutboundCall(String destination);

  void hangUp(String callId);

  void answer(String callId);
}
