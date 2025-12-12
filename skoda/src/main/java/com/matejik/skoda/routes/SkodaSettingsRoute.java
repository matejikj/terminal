package com.matejik.skoda.routes;

import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.layout.TerminalLayout;
import com.matejik.terminal.sip.SipTerminalService;
import com.matejik.terminal.ui.views.SettingsView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "nastaveni", layout = TerminalLayout.class)
@PageTitle("Nastavení")
public class SkodaSettingsRoute extends SettingsView {

  public SkodaSettingsRoute(
      TerminalLocaleService localeService, SipTerminalService sipTerminalService) {
    super(localeService, sipTerminalService);
  }
}
