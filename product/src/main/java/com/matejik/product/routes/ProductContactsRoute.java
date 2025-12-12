package com.matejik.product.routes;

import com.matejik.terminal.ui.view.ContactsView;
import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "contacts", layout = TerminalLayout.class)
@PageTitle("Contacts")
public class ProductContactsRoute extends ContactsView {}
