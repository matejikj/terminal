package com.matejik.terminal.ui.component;

import com.matejik.terminal.infrastructure.audio.AudioDeviceAdapter;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public final class QuickActionsSidebar extends Composite<Div> {

  private final AudioDeviceAdapter audioDeviceAdapter;

  public QuickActionsSidebar(AudioDeviceAdapter audioDeviceAdapter) {
    this.audioDeviceAdapter = audioDeviceAdapter;

    var root = getContent();
    root.addClassNames("terminal-right-sidebar", LumoUtility.Padding.SMALL);
    var container = new VerticalLayout();
    container.addClassNames(
        "quick-actions-stack",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.Margin.NONE);
    container.setPadding(false);

    container.add(
        createPrimaryAction(VaadinIcon.VOLUME_OFF, "terminal.actions.mute", () -> {}),
        createPrimaryAction(VaadinIcon.SUN_O, "terminal.actions.theme", () -> {}),
        createPrimaryAction(VaadinIcon.BELL, "terminal.actions.notifications", () -> {}),
        createPrimaryAction(
            VaadinIcon.VOLUME_UP,
            "terminal.actions.test-sound",
            audioDeviceAdapter::playTestSound));

    root.add(container);
  }

  private Button createPrimaryAction(
      VaadinIcon icon, String translationKey, Runnable onClickAction) {
    var button = new Button(icon.create(), event -> onClickAction.run());
    button.setTooltipText(getTranslation(translationKey));
    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
    button.addClassNames("quick-action");
    return button;
  }
}
