package com.matejik.terminal.i18n;

import com.matejik.terminal.config.TerminalProperties;
import com.matejik.terminal.state.StateStore;
import com.matejik.terminal.state.TerminalStateKeys;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@VaadinSessionScope
public class TerminalLocaleService {

    private final StateStore stateStore;
    private final TerminalProperties properties;

    public TerminalLocaleService(StateStore stateStore, TerminalProperties properties) {
        this.stateStore = stateStore;
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        if (stateStore.get(TerminalStateKeys.ACTIVE_LOCALE) == null) {
            stateStore.set(TerminalStateKeys.ACTIVE_LOCALE, properties.ui().getDefaultLocale());
        }
    }

    public Locale currentLocale() {
        return Optional.ofNullable(stateStore.get(TerminalStateKeys.ACTIVE_LOCALE))
                .orElse(properties.ui().getDefaultLocale());
    }

    public void switchLocale(Locale locale) {
        stateStore.set(TerminalStateKeys.ACTIVE_LOCALE, locale);
        var ui = UI.getCurrent();
        if (ui != null) {
            ui.setLocale(locale);
        }
    }
}
