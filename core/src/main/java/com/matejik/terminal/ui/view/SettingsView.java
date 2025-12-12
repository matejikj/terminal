package com.matejik.terminal.ui.view;

import com.matejik.sip.SipAccountCredentials;
import com.matejik.terminal.application.command.RegistrationCommandService;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.Locale;

public class SettingsView extends Composite<Div> {

  private final TerminalLocaleService localeService;
  private final RegistrationCommandService registrationService;

  public SettingsView(
      TerminalLocaleService localeService, RegistrationCommandService registrationService) {
    this.localeService = localeService;
    this.registrationService = registrationService;
    var root = getContent();
    root.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.LARGE,
        LumoUtility.MaxWidth.SCREEN_MEDIUM);

    root.add(buildLocalizationCard(), buildSipCard());
  }

  private Div buildLocalizationCard() {
    var card = new Div();
    card.addClassNames(
        "settings-card",
        LumoUtility.Padding.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.BoxShadow.SMALL);
    var title = new H4(getTranslation("terminal.settings.language"));
    var languageSelect = new Select<Locale>();
    languageSelect.setItems(Locale.ENGLISH, new Locale("cs", "CZ"));
    languageSelect.setValue(localeService.currentLocale());
    languageSelect.setItemLabelGenerator(locale -> locale.getDisplayLanguage(locale));
    languageSelect.addValueChangeListener(
        event -> {
          if (event.getValue() != null) {
            localeService.switchLocale(event.getValue());
          }
        });
    card.add(title, languageSelect);
    return card;
  }

  private Div buildSipCard() {
    var card = new Div();
    card.addClassNames(
        "settings-card",
        LumoUtility.Padding.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.BoxShadow.SMALL,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.SMALL);
    var title = new H4(getTranslation("terminal.settings.sip"));

    var displayName = new TextField(getTranslation("terminal.settings.displayName"));
    var username = new TextField(getTranslation("terminal.settings.username"));
    var domain = new TextField(getTranslation("terminal.settings.domain"));
    var password = new PasswordField(getTranslation("terminal.settings.password"));

    var form = new FormLayout(displayName, username, domain, password);
    form.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));

    var connectButton =
        new Button(
            getTranslation("terminal.settings.connect"),
            event -> {
              var credentials =
                  new SipAccountCredentials(
                      displayName.getValue(),
                      username.getValue(),
                      domain.getValue(),
                      password.getValue());
              registrationService.connect(credentials);
            });
    var disconnectButton =
        new Button(
            getTranslation("terminal.settings.disconnect"),
            event -> registrationService.disconnect());
    disconnectButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);

    card.add(title, form, new Div(connectButton, disconnectButton));
    return card;
  }
}
