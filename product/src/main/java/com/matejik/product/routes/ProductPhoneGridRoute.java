package com.matejik.product.routes;

import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.call.command.CallCommandService;
import com.matejik.terminal.ui.view.PhoneGridView;
import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "phone", layout = TerminalLayout.class)
@RouteAlias(value = "", layout = TerminalLayout.class)
@PageTitle("Phone")
public class ProductPhoneGridRoute extends PhoneGridView {

  public ProductPhoneGridRoute(AppStore appStore, CallCommandService callCommandService) {
    super(appStore, callCommandService);
  }
}
