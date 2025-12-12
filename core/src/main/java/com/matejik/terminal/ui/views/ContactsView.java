package com.matejik.terminal.ui.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class ContactsView extends Composite<Div> {

  public ContactsView() {
    var root = getContent();
    root.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM);
    root.add(new Paragraph(getTranslation("terminal.contacts.placeholder")));
  }
}
