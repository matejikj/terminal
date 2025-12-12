package com.matejik.terminal.navigation;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class TerminalNavigationRegistry {

  private final List<TerminalNavItem> navItems;

  public TerminalNavigationRegistry(ObjectProvider<TerminalNavigationProvider> providers) {
    this.navItems = providers.stream().flatMap(provider -> provider.items().stream()).toList();
  }

  public List<TerminalNavItem> items() {
    return navItems;
  }
}
