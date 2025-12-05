package com.matejik.terminal.navigation;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TerminalNavigationRegistry {

    private final List<TerminalNavItem> navItems;

    public TerminalNavigationRegistry(ObjectProvider<TerminalNavigationProvider> providers) {
        this.navItems = providers.stream()
                .flatMap(provider -> provider.items().stream())
                .toList();
    }

    public List<TerminalNavItem> items() {
        return navItems;
    }
}
