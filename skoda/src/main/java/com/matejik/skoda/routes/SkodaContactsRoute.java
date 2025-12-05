package com.matejik.skoda.routes;

import com.matejik.terminal.layout.TerminalLayout;
import com.matejik.terminal.ui.views.ContactsView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "kontakty", layout = TerminalLayout.class)
@PageTitle("Kontakty")
public class SkodaContactsRoute extends ContactsView {
}
