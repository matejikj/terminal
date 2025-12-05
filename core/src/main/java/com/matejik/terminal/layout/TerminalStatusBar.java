package com.matejik.terminal.layout;

import com.matejik.terminal.brand.BrandProfile;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.sip.SipSessionState;
import com.matejik.terminal.sip.SipTerminalService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Locale;

public final class TerminalStatusBar extends Composite<Div> {

    private final SipTerminalService sipService;
    private final TerminalLocaleService localeService;
    private Registration sipRegistration;
    private final Span statusLabel = new Span();
    private final Span accountLabel = new Span();

    public TerminalStatusBar(BrandProfile brandProfile, SipTerminalService sipService, TerminalLocaleService localeService) {
        this.sipService = sipService;
        this.localeService = localeService;
        var root = getContent();
        root.addClassNames("terminal-status-bar", LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.BETWEEN, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.XSMALL);

        var branding = new Div(brandProfile.logo(), new Span(brandProfile.name()));
        branding.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL,
                LumoUtility.FontWeight.BOLD);

        var statusSection = new Div(statusLabel, accountLabel);
        statusSection.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.XSMALL);

        var languageToggle = new Button(getTranslation("terminal.status.language"), event -> toggleLocale());
        languageToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);

        root.add(branding, statusSection, languageToggle);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        sipRegistration = sipService.observeSessionState(state ->
                attachEvent.getUI().access(() -> applyState(state)));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (sipRegistration != null) {
            sipRegistration.remove();
            sipRegistration = null;
        }
    }

    private void applyState(SipSessionState state) {
        var statusKey = "terminal.status." + state.connectionStatus().name().toLowerCase(Locale.ROOT);
        statusLabel.setText(getTranslation(statusKey));
        var account = state.activeAccount()
                .map(credentials -> credentials.displayName() + " (" + credentials.toSipUri() + ')')
                .orElse(getTranslation("terminal.status.noAccount"));
        accountLabel.setText(account);
    }

    private void toggleLocale() {
        var current = localeService.currentLocale();
        var next = current.getLanguage().equals("cs") ? Locale.ENGLISH : new Locale("cs", "CZ");
        localeService.switchLocale(next);
    }
}
