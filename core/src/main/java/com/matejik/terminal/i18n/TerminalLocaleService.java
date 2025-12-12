package com.matejik.terminal.i18n;

import com.matejik.terminal.config.TerminalProperties;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class TerminalLocaleService {

  private final TerminalProperties properties;
  private final AtomicReference<Locale> currentLocale = new AtomicReference<>();
  private final CopyOnWriteArrayList<Consumer<Locale>> listeners = new CopyOnWriteArrayList<>();

  public TerminalLocaleService(TerminalProperties properties) {
    this.properties = properties;
  }

  @PostConstruct
  void init() {
    currentLocale.compareAndSet(null, properties.ui().getDefaultLocale());
  }

  public Locale currentLocale() {
    var locale = currentLocale.get();
    return locale != null ? locale : properties.ui().getDefaultLocale();
  }

  public Registration addListener(Consumer<Locale> listener) {
    listeners.add(listener);
    listener.accept(currentLocale());
    return () -> listeners.remove(listener);
  }

  public void switchLocale(Locale locale) {
    currentLocale.set(locale);
    listeners.forEach(listener -> listener.accept(locale));
    var ui = UI.getCurrent();
    if (ui != null) {
      ui.setLocale(locale);
    }
  }
}
