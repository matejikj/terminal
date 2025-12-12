package com.matejik.terminal.layout;

import com.matejik.terminal.application.state.AppState;
import com.matejik.terminal.application.state.CallSlice.CallPhase;
import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.brand.BrandProfile;
import com.matejik.terminal.domain.registration.RegistrationStatus;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TerminalStatusBar extends Composite<Div> {

  private final AppStore appStore;
  private final TerminalLocaleService localeService;
  private Registration storeRegistration;
  private final Span statusLabel = new Span();
  private final Span accountLabel = new Span();
  private final Span clockLabel = new Span();
  private final StatusTag pbxTag = new StatusTag("PBX");
  private final StatusTag recordingTag = new StatusTag("Nahrávání");
  private final StatusTag loggingTag = new StatusTag("Logování");
  private final DateTimeFormatter clockFormatter =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss 'UTC'");
  private static final Duration SYNC_INTERVAL = Duration.ofMinutes(1);
  private ZonedDateTime referenceTime;
  private long ticksSinceSync = Long.MAX_VALUE;
  private Registration clockPollRegistration;

  public TerminalStatusBar(
      BrandProfile brandProfile, AppStore appStore, TerminalLocaleService localeService) {
    this.appStore = appStore;
    this.localeService = localeService;
    var root = getContent();
    root.addClassNames(
        "terminal-status-bar",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.JustifyContent.BETWEEN,
        LumoUtility.Gap.MEDIUM);

    var branding = new Div(brandProfile.logo(), new Span(brandProfile.name()));
    branding.addClassNames(
        "terminal-status-brand",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Gap.SMALL,
        LumoUtility.FontWeight.BOLD);

    var statusSummary = new Div(statusLabel, accountLabel);
    statusSummary.addClassNames(
        "terminal-status-summary",
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.XSMALL);

    var tags = new Div(pbxTag, recordingTag, loggingTag);
    tags.addClassNames(
        "terminal-status-tags",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Gap.SMALL);

    var center = new Div(tags);
    center.addClassNames(
        "terminal-status-center",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Gap.SMALL);

    clockLabel.addClassNames("terminal-status-clock");

    var languageToggle =
        new Button(getTranslation("terminal.status.language"), event -> toggleLocale());
    languageToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
    languageToggle.addClassNames("terminal-status-language");

    var rightCluster = new Div(clockLabel, languageToggle);
    rightCluster.addClassNames(
        "terminal-status-right",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Gap.SMALL);

    root.add(branding, center, statusSummary, rightCluster);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    storeRegistration =
        appStore.subscribe(state -> attachEvent.getUI().access(() -> applyState(state)));
    startClock(attachEvent);
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    if (storeRegistration != null) {
      storeRegistration.remove();
      storeRegistration = null;
    }
    stopClock(detachEvent.getUI());
  }

  private void applyState(AppState state) {
    var registration = state.registrationSlice();
    var statusKey = "terminal.status." + registration.status().name().toLowerCase(Locale.ROOT);
    statusLabel.setText(getTranslation(statusKey));

    var account =
        registration.accountLabel().isBlank()
            ? getTranslation("terminal.status.noAccount")
            : registration.accountLabel();
    accountLabel.setText(account);

    var connected = registration.status() == RegistrationStatus.REGISTERED;
    var hasCall = !state.callSlice().activeCalls().isEmpty();
    var recording =
        state.callSlice().activeCalls().stream().anyMatch(call -> call.phase() == CallPhase.ACTIVE);

    pbxTag.setActive(connected);
    recordingTag.setActive(recording);
    loggingTag.setActive(hasCall);
  }

  private void toggleLocale() {
    var current = localeService.currentLocale();
    var next = current.getLanguage().equals("cs") ? Locale.ENGLISH : new Locale("cs", "CZ");
    localeService.switchLocale(next);
  }

  private void startClock(AttachEvent attachEvent) {
    stopClock(attachEvent.getUI());
    var ui = attachEvent.getUI();
    ui.setPollInterval(1000);
    referenceTime = null;
    ticksSinceSync = Long.MAX_VALUE;
    updateClock();
    clockPollRegistration = ui.addPollListener(event -> updateClock());
  }

  private void stopClock(UI ui) {
    if (clockPollRegistration != null) {
      clockPollRegistration.remove();
      clockPollRegistration = null;
    }
    if (ui != null) {
      ui.setPollInterval(-1);
    }
  }

  private void updateClock() {
    if (referenceTime == null || ticksSinceSync >= SYNC_INTERVAL.getSeconds()) {
      referenceTime = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
      ticksSinceSync = 0;
    } else {
      referenceTime = referenceTime.plusSeconds(1);
      ticksSinceSync++;
    }
    clockLabel.setText(clockFormatter.format(referenceTime));
  }

  private static final class StatusTag extends Div {
    private final Div led = new Div();
    private final Span label = new Span();

    private StatusTag(String text) {
      addClassName("status-tag");
      led.addClassName("status-tag__led");
      label.addClassName("status-tag__label");
      label.setText(text);
      add(led, label);
    }

    private void setActive(boolean active) {
      getElement().setAttribute("data-state", active ? "on" : "off");
    }
  }
}
