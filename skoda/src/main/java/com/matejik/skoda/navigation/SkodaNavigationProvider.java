package com.matejik.skoda.navigation;

import com.matejik.skoda.routes.SkodaContactsRoute;
import com.matejik.skoda.routes.SkodaPhoneGridRoute;
import com.matejik.skoda.routes.SkodaSettingsRoute;
import com.matejik.terminal.navigation.TerminalNavItem;
import com.matejik.terminal.navigation.TerminalNavigationProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkodaNavigationProvider implements TerminalNavigationProvider {

    @Override
    public List<TerminalNavItem> items() {
        return List.of(
                new TerminalNavItem("terminal.menu.phone", VaadinIcon.PHONE::create, SkodaPhoneGridRoute.class),
                new TerminalNavItem("terminal.menu.contacts", VaadinIcon.GROUP::create, SkodaContactsRoute.class),
                new TerminalNavItem("terminal.menu.settings", VaadinIcon.COG::create, SkodaSettingsRoute.class)
        );
    }
}
