package com.matejik.terminal.config;

import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.LoadDependenciesOnStartup;
import com.vaadin.flow.theme.Theme;

@Theme(value = "terminal")
@LoadDependenciesOnStartup(TerminalLayout.class)
public class TerminalAppShell implements AppShellConfigurator {
}
