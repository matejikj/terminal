package com.matejik.product.routes;

import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.layout.TerminalLayout;
import com.matejik.terminal.sip.SipTerminalService;
import com.matejik.terminal.ui.views.SettingsView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "settings", layout = TerminalLayout.class)
@PageTitle("Settings")
public class ProductSettingsRoute extends SettingsView {

  public ProductSettingsRoute(
      TerminalLocaleService localeService, SipTerminalService sipTerminalService) {
    super(localeService, sipTerminalService);
  }
}
