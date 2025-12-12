package com.matejik.terminal.application.state.actions;

/**
 * Marker for all commands/actions that the {@link com.matejik.terminal.application.store.AppStore}
 * can process. Sealing across packages would require JPMS modules, so we keep it open and rely on
 * package conventions.
 */
public interface AppAction {}
