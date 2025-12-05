package com.matejik.terminal.config;

import com.vaadin.flow.i18n.I18NProvider;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class TerminalI18NProvider implements I18NProvider {

    private static final String BUNDLE_NAME = "i18n.messages";
    private final List<Locale> providedLocales = List.of(Locale.ENGLISH, new Locale("cs", "CZ"));
    private final Locale fallbackLocale;

    public TerminalI18NProvider(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale == null ? Locale.ENGLISH : fallbackLocale;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return providedLocales;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        Locale effectiveLocale = locale == null ? fallbackLocale : locale;
        try {
            var bundle = ResourceBundle.getBundle(BUNDLE_NAME, effectiveLocale);
            var value = bundle.getString(key);
            return params == null || params.length == 0 ? value : MessageFormat.format(value, params);
        } catch (MissingResourceException ex) {
            return key;
        }
    }
}
