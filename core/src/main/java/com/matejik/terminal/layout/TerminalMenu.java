package com.matejik.terminal.layout;

import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.navigation.TerminalNavItem;
import com.matejik.terminal.navigation.TerminalNavigationRegistry;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.ArrayList;
import java.util.List;

public final class TerminalMenu extends Composite<Div> implements AfterNavigationListener {

  private final List<MenuEntry> entries = new ArrayList<>();
  private final TerminalLocaleService localeService;
  private Registration localeRegistration;
  private Registration navigationRegistration;

  public TerminalMenu(
      TerminalNavigationRegistry navigationRegistry, TerminalLocaleService localeService) {
    this.localeService = localeService;
    var container = getContent();
    container.addClassNames(
        "terminal-left-sidebar",
        LumoUtility.Padding.SMALL,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Gap.SMALL);

    var stack = new Div();
    stack.addClassNames(
        "terminal-sidebar-buttons",
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.SMALL);
    navigationRegistry.items().forEach(item -> stack.add(createNavButton(item)));

    container.add(stack);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    updateLocale();
    localeRegistration =
        localeService.addListener(locale -> attachEvent.getUI().access(this::updateLocale));
    navigationRegistration = attachEvent.getUI().addAfterNavigationListener(this);
    attachEvent.getUI().access(this::refreshSelection);
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    if (localeRegistration != null) {
      localeRegistration.remove();
      localeRegistration = null;
    }
    if (navigationRegistration != null) {
      navigationRegistration.remove();
      navigationRegistration = null;
    }
  }

  private Button createNavButton(TerminalNavItem item) {
    var iconComponent = item.icon().get();
    if (iconComponent instanceof Icon icon) {
      icon.setSize("24px");
      icon.getElement().getClassList().add("terminal-sidebar-icon");
    }
    var button = new Button(iconComponent);
    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
    button.addClassNames("terminal-sidebar-button");
    button.addClickListener(
        event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(item.navigationTarget())));
    entries.add(new MenuEntry(item, button));
    return button;
  }

  private void updateLocale() {
    var ui = getUI().orElse(null);
    if (ui == null) {
      return;
    }
    entries.forEach(
        entry -> {
          var translated = ui.getTranslation(entry.item.translationKey());
          entry.button.setTooltipText(translated);
          entry.button.getElement().setAttribute("aria-label", translated);
        });
  }

  private void refreshSelection() {
    getUI()
        .ifPresent(
            ui -> {
              var chain = ui.getInternals().getActiveRouterTargetsChain();
              if (!chain.isEmpty()) {
                var target = chain.get(chain.size() - 1).getClass();
                setActiveTarget(target);
              }
            });
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    var chain = event.getActiveChain();
    if (!chain.isEmpty()) {
      setActiveTarget(chain.get(chain.size() - 1).getClass());
    }
  }

  private void setActiveTarget(Class<?> target) {
    entries.forEach(
        entry -> {
          var active = target != null && target.equals(entry.item.navigationTarget());
          entry.button.getElement().getClassList().set("is-active", active);
        });
  }

  private record MenuEntry(TerminalNavItem item, Button button) {}
}
