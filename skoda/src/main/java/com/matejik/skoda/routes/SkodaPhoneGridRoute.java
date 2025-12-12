package com.matejik.skoda.routes;

import com.matejik.terminal.layout.TerminalLayout;
import com.matejik.terminal.sip.SipTerminalService;
import com.matejik.terminal.ui.views.PhoneGridView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "telefon", layout = TerminalLayout.class)
@RouteAlias(value = "", layout = TerminalLayout.class)
@PageTitle("Telefon")
public class SkodaPhoneGridRoute extends PhoneGridView {

  public SkodaPhoneGridRoute(SipTerminalService service) {
    super(service);
  }
}
