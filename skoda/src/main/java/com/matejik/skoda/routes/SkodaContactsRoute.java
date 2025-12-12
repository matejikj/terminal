package com.matejik.skoda.routes;

import com.matejik.terminal.ui.view.ContactsView;
import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "kontakty", layout = TerminalLayout.class)
@PageTitle("Kontakty")
public class SkodaContactsRoute extends ContactsView {}
