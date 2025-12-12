package com.matejik.skoda.routes;

import com.matejik.terminal.domain.registration.command.RegistrationCommandService;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.ui.view.SettingsView;
import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "nastaveni", layout = TerminalLayout.class)
@PageTitle("Nastavení")
public class SkodaSettingsRoute extends SettingsView {

  public SkodaSettingsRoute(
      TerminalLocaleService localeService, RegistrationCommandService registrationCommandService) {
    super(localeService, registrationCommandService);
  }
}
