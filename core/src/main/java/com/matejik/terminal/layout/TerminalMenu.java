package com.matejik.terminal.layout;

import com.matejik.terminal.navigation.TerminalNavItem;
import com.matejik.terminal.navigation.TerminalNavigationRegistry;
import com.matejik.terminal.state.StateStore;
import com.matejik.terminal.state.TerminalStateKeys;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.ArrayList;
import java.util.List;

public final class TerminalMenu extends Composite<Div> {

    private final List<MenuEntry> entries = new ArrayList<>();
    private final StateStore stateStore;
    private Registration localeRegistration;

    public TerminalMenu(TerminalNavigationRegistry navigationRegistry, StateStore stateStore) {
        this.stateStore = stateStore;
        var container = getContent();
        container.addClassNames("terminal-left-sidebar", LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);

        var nav = new SideNav();
        nav.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);
        navigationRegistry.items().forEach(item -> nav.addItem(createNavItem(item)));

        container.add(nav);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        updateLocale();
        localeRegistration = stateStore.addListener(TerminalStateKeys.ACTIVE_LOCALE, locale ->
                attachEvent.getUI().access(this::updateLocale));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (localeRegistration != null) {
            localeRegistration.remove();
            localeRegistration = null;
        }
    }

    private SideNavItem createNavItem(TerminalNavItem item) {
        var iconComponent = item.icon().get();
        if (iconComponent instanceof Icon icon) {
            icon.setSize("var(--lumo-size-m)");
        }
        var navItem = new SideNavItem("", item.navigationTarget());
        navItem.setPrefixComponent(iconComponent);
        entries.add(new MenuEntry(item, navItem));
        return navItem;
    }

    private void updateLocale() {
        var ui = getUI().orElse(null);
        if (ui == null) {
            return;
        }
        entries.forEach(entry -> entry.component.setLabel(ui.getTranslation(entry.item.translationKey())));
    }

    private record MenuEntry(TerminalNavItem item, SideNavItem component) {
    }
}
