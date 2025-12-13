package com.matejik.product.routes;

import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.registration.command.RegistrationCommandService;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.infrastructure.audio.AudioDeviceAdapter;
import com.matejik.terminal.ui.view.SettingsView;
import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "settings", layout = TerminalLayout.class)
@PageTitle("Settings")
public class ProductSettingsRoute extends SettingsView {

  public ProductSettingsRoute(
      TerminalLocaleService localeService,
      RegistrationCommandService registrationCommandService,
      AppStore appStore,
      AudioDeviceAdapter audioDeviceAdapter) {
    super(localeService, registrationCommandService, appStore, audioDeviceAdapter);
  }
}
