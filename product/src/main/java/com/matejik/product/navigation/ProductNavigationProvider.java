package com.matejik.product.navigation;

import com.matejik.product.routes.ProductContactsRoute;
import com.matejik.product.routes.ProductPhoneGridRoute;
import com.matejik.product.routes.ProductSettingsRoute;
import com.matejik.terminal.navigation.TerminalNavItem;
import com.matejik.terminal.navigation.TerminalNavigationProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductNavigationProvider implements TerminalNavigationProvider {

  @Override
  public List<TerminalNavItem> items() {
    return List.of(
        new TerminalNavItem(
            "terminal.menu.phone", VaadinIcon.PHONE::create, ProductPhoneGridRoute.class),
        new TerminalNavItem(
            "terminal.menu.contacts", VaadinIcon.GROUP::create, ProductContactsRoute.class),
        new TerminalNavItem(
            "terminal.menu.settings", VaadinIcon.COG::create, ProductSettingsRoute.class));
  }
}
