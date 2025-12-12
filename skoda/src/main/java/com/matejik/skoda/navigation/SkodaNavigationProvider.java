package com.matejik.skoda.navigation;

import com.matejik.skoda.routes.SkodaContactsRoute;
import com.matejik.skoda.routes.SkodaPhoneGridRoute;
import com.matejik.skoda.routes.SkodaSettingsRoute;
import com.matejik.terminal.application.navigation.TerminalNavItem;
import com.matejik.terminal.application.navigation.TerminalNavigationProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SkodaNavigationProvider implements TerminalNavigationProvider {

  @Override
  public List<TerminalNavItem> items() {
    return List.of(
        new TerminalNavItem(
            "terminal.menu.phone", VaadinIcon.PHONE::create, SkodaPhoneGridRoute.class),
        new TerminalNavItem(
            "terminal.menu.contacts", VaadinIcon.GROUP::create, SkodaContactsRoute.class),
        new TerminalNavItem(
            "terminal.menu.settings", VaadinIcon.COG::create, SkodaSettingsRoute.class));
  }
}
