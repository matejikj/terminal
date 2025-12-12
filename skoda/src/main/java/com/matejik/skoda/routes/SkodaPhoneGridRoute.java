package com.matejik.skoda.routes;

import com.matejik.terminal.application.command.CallCommandService;
import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.ui.view.PhoneGridView;
import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "telefon", layout = TerminalLayout.class)
@RouteAlias(value = "", layout = TerminalLayout.class)
@PageTitle("Telefon")
public class SkodaPhoneGridRoute extends PhoneGridView {

  public SkodaPhoneGridRoute(AppStore appStore, CallCommandService callCommandService) {
    super(appStore, callCommandService);
  }
}
