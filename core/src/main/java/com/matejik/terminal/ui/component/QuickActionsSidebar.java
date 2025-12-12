package com.matejik.terminal.ui.component;

import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.audio.state.AudioSlice;
import com.matejik.terminal.infrastructure.audio.AudioDeviceAdapter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

public final class QuickActionsSidebar extends Composite<Div> {

  private final AudioDeviceAdapter audioDeviceAdapter;
  private final AppStore appStore;
  private final Button muteButton;
  private final Input volumeSlider;
  private Registration audioStoreRegistration;
  private boolean muted;

  public QuickActionsSidebar(AppStore appStore, AudioDeviceAdapter audioDeviceAdapter) {
    this.audioDeviceAdapter = audioDeviceAdapter;
    this.appStore = appStore;

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

    muteButton = createMuteAction();
    updateMuteButton();

    volumeSlider = createVolumeSlider();

    container.add(
        muteButton,
        createPrimaryAction(VaadinIcon.SUN_O, "terminal.actions.theme", () -> {}),
        createPrimaryAction(VaadinIcon.BELL, "terminal.actions.notifications", () -> {}),
        createVolumeControl());

    root.add(container);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    if (audioStoreRegistration == null) {
      audioStoreRegistration =
          appStore.subscribe(
              state ->
                  attachEvent.getUI().access(() -> applyAudioState(state.audioSlice())));
    }
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (audioStoreRegistration != null) {
      audioStoreRegistration.remove();
      audioStoreRegistration = null;
    }
    super.onDetach(detachEvent);
  }

  private void applyAudioState(AudioSlice audioSlice) {
    muted = audioSlice.muted();
    updateMuteButton();
  }

  private Div createVolumeControl() {
    var wrapper = new Div(volumeSlider);
    wrapper.addClassNames(
        "quick-actions-volume",
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.JustifyContent.CENTER);
    return wrapper;
  }

  private Button createMuteAction() {
    var button = new Button();
    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
    button.addClassNames("quick-action");
    button.addClickListener(event -> toggleMute());
    return button;
  }

  private Input createVolumeSlider() {
    var slider = new Input();
    slider.setType("range");
    slider.getElement().setAttribute("min", "0");
    slider.getElement().setAttribute("max", "100");
    slider.getElement().setAttribute("step", "1");
    slider.setValue(String.valueOf(Math.round(audioDeviceAdapter.currentOutputVolume() * 100d)));
    slider.getElement().setProperty("orient", "vertical");
    slider.getElement().setAttribute("title", getTranslation("terminal.actions.volume"));
    slider.addClassNames("volume-slider");
    slider.addValueChangeListener(
        event -> {
          if (!event.isFromClient()) {
            return;
          }
          var value = event.getValue();
          if (value == null || value.isBlank()) {
            return;
          }
          try {
            var parsed = Double.parseDouble(value);
            audioDeviceAdapter.setOutputVolume(parsed / 100d);
          } catch (NumberFormatException ignored) {
          }
        });
    return slider;
  }

  private void toggleMute() {
    audioDeviceAdapter.mute(!muted);
  }

  private void updateMuteButton() {
    var icon = muted ? VaadinIcon.VOLUME_OFF.create() : VaadinIcon.VOLUME_UP.create();
    muteButton.setIcon(icon);
    var translationKey = muted ? "terminal.actions.unmute" : "terminal.actions.mute";
    muteButton.setTooltipText(getTranslation(translationKey));
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
