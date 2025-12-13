package com.matejik.terminal.config;

import com.matejik.terminal.ui.view.TerminalLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.LoadDependenciesOnStartup;
import com.vaadin.flow.theme.Theme;

@Theme(value = "terminal")
@LoadDependenciesOnStartup(TerminalLayout.class)
@Uses(TextField.class)
@Uses(PasswordField.class)
@Uses(FormLayout.class)
@Uses(Select.class)
public class TerminalAppShell implements AppShellConfigurator {
}
