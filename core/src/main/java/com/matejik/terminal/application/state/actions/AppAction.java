package com.matejik.terminal.application.state.actions;

public sealed interface AppAction permits AudioAction, CallAction, RegistrationAction {}
