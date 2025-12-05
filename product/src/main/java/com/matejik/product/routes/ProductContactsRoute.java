package com.matejik.product.routes;

import com.matejik.terminal.layout.TerminalLayout;
import com.matejik.terminal.ui.views.ContactsView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "contacts", layout = TerminalLayout.class)
@PageTitle("Contacts")
public class ProductContactsRoute extends ContactsView {
}
