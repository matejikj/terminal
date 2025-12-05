package com.matejik.product.routes;

import com.matejik.terminal.layout.TerminalLayout;
import com.matejik.terminal.sip.SipTerminalService;
import com.matejik.terminal.ui.views.PhoneGridView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "phone", layout = TerminalLayout.class)
@RouteAlias(value = "", layout = TerminalLayout.class)
@PageTitle("Phone")
public class ProductPhoneGridRoute extends PhoneGridView {

    public ProductPhoneGridRoute(SipTerminalService sipTerminalService) {
        super(sipTerminalService);
    }
}
