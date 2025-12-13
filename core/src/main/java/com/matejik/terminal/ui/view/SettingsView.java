package com.matejik.terminal.ui.view;

import com.matejik.sip.SipAccountCredentials;
import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.audio.AudioDeviceType;
import com.matejik.terminal.domain.audio.state.AudioAction;
import com.matejik.terminal.domain.audio.state.AudioSlice;
import com.matejik.terminal.domain.registration.command.RegistrationCommandService;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.infrastructure.audio.AudioDeviceAdapter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.Locale;
import java.util.Objects;

public class SettingsView extends Composite<Div> {

  private final TerminalLocaleService localeService;
  private final RegistrationCommandService registrationService;
  private final AppStore appStore;
  private final AudioDeviceAdapter audioDeviceAdapter;
  private Registration audioStoreRegistration;
  private final Select<AudioSlice.AudioDevice> primaryOutputSelect = new Select<>();
  private final Select<AudioSlice.AudioDevice> secondaryOutputSelect = new Select<>();
  private final Select<AudioSlice.AudioDevice> microphoneSelect = new Select<>();
  private final Button testPrimaryButton = new Button();
  private final Button testSecondaryButton = new Button();
  private final Button micTestButton = new Button();
  private final Button microphoneMuteButton = new Button();
  private final Div micTestIndicator = new Div();
  private final Span micTestIndicatorText = new Span();
  private Registration micTestListenerRegistration;
  private Registration microphoneMuteListenerRegistration;
  private boolean micTestRecording;
  private boolean microphoneMuted;

  public SettingsView(
      TerminalLocaleService localeService,
      RegistrationCommandService registrationService,
      AppStore appStore,
      AudioDeviceAdapter audioDeviceAdapter) {
    this.localeService = localeService;
    this.registrationService = registrationService;
    this.appStore = appStore;
    this.audioDeviceAdapter = audioDeviceAdapter;
    var root = getContent();
    root.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.LARGE,
        LumoUtility.MaxWidth.SCREEN_MEDIUM);

    root.add(buildLocalizationCard(), buildAudioCard(), buildSipCard());
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

  private Div buildAudioCard() {
    var card = new Div();
    card.addClassNames(
        "settings-card",
        LumoUtility.Padding.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.BoxShadow.SMALL,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Gap.MEDIUM);
    var title = new H4(getTranslation("terminal.settings.audio"));
    title.addClassNames(LumoUtility.Margin.NONE);

    primaryOutputSelect.setLabel(getTranslation("terminal.settings.audio.primary"));
    primaryOutputSelect.setWidthFull();
    primaryOutputSelect.setItemLabelGenerator(device -> device == null ? "" : device.label());
    primaryOutputSelect.addValueChangeListener(
        event -> {
          if (!event.isFromClient()) {
            return;
          }
          var device = event.getValue();
          if (device != null) {
            audioDeviceAdapter.selectDevice(AudioDeviceType.OUTPUT, device.id());
          }
        });

    secondaryOutputSelect.setLabel(getTranslation("terminal.settings.audio.secondary"));
    secondaryOutputSelect.setWidthFull();
    secondaryOutputSelect.setItemLabelGenerator(
        device ->
            device == null
                ? getTranslation("terminal.settings.audio.secondary.none")
                : device.label());
    secondaryOutputSelect.setEmptySelectionAllowed(true);
    secondaryOutputSelect.setEmptySelectionCaption(
        getTranslation("terminal.settings.audio.secondary.none"));
    secondaryOutputSelect.setPlaceholder(getTranslation("terminal.settings.audio.secondary.none"));
    secondaryOutputSelect.addValueChangeListener(
        event -> {
          if (!event.isFromClient()) {
            return;
          }
          var value = event.getValue();
          var deviceId = value != null ? value.id() : null;
          appStore.dispatch(new AudioAction.UpdateSecondaryOutput(deviceId));
        });

    testPrimaryButton.setText(getTranslation("terminal.settings.audio.testPrimary"));
    testPrimaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    testPrimaryButton.addClickListener(event -> playPrimaryTestSound());
    testPrimaryButton.setEnabled(false);

    testSecondaryButton.setText(getTranslation("terminal.settings.audio.testSecondary"));
    testSecondaryButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    testSecondaryButton.addClickListener(event -> playSecondaryTestSound());
    testSecondaryButton.setEnabled(false);

    microphoneSelect.setLabel(getTranslation("terminal.settings.audio.microphone"));
    microphoneSelect.setWidthFull();
    microphoneSelect.setItemLabelGenerator(device -> device == null ? "" : device.label());
    microphoneSelect.addValueChangeListener(
        event -> {
          if (!event.isFromClient()) {
            return;
          }
          var device = event.getValue();
          if (device != null) {
            audioDeviceAdapter.selectDevice(AudioDeviceType.INPUT, device.id());
          }
        });

    microphoneMuteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    microphoneMuteButton.addClickListener(event -> toggleMicrophoneMute());
    updateMicrophoneMuteButton();

    micTestIndicator.addClassNames("mic-test-indicator");
    micTestIndicator.setVisible(false);
    var pulse = new Div();
    pulse.addClassNames("mic-test-indicator__pulse");
    micTestIndicatorText.addClassNames("mic-test-indicator__text");
    micTestIndicator.add(pulse, micTestIndicatorText);

    micTestButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    micTestButton.addClickListener(event -> toggleMicTestRecording());
    micTestButton.setEnabled(false);

    var buttonRow = new Div(testPrimaryButton, testSecondaryButton);
    buttonRow.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.Gap.SMALL,
        LumoUtility.FlexWrap.WRAP,
        LumoUtility.Margin.NONE,
        LumoUtility.Padding.NONE);

    card.add(
        title,
        primaryOutputSelect,
        secondaryOutputSelect,
        buttonRow,
        microphoneSelect,
        microphoneMuteButton,
        micTestButton,
        micTestIndicator);
    updateMicTestControls();
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

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    if (audioStoreRegistration == null) {
      audioStoreRegistration =
          appStore.subscribe(
              state -> attachEvent.getUI().access(() -> applyAudioState(state.audioSlice())));
    }
    if (micTestListenerRegistration == null) {
      micTestListenerRegistration =
          audioDeviceAdapter.addMicTestStateListener(
              recording ->
                  attachEvent
                      .getUI()
                      .access(
                          () -> {
                            micTestRecording = recording;
                            updateMicTestControls();
                          }));
    }
    if (microphoneMuteListenerRegistration == null) {
      microphoneMuteListenerRegistration =
          audioDeviceAdapter.addMicrophoneMuteListener(
              muted ->
                  attachEvent
                      .getUI()
                      .access(
                          () -> {
                            microphoneMuted = muted;
                            updateMicrophoneMuteButton();
                            updateMicTestControls();
                          }));
    }
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (audioStoreRegistration != null) {
      audioStoreRegistration.remove();
      audioStoreRegistration = null;
    }
    if (micTestListenerRegistration != null) {
      micTestListenerRegistration.remove();
      micTestListenerRegistration = null;
    }
    if (microphoneMuteListenerRegistration != null) {
      microphoneMuteListenerRegistration.remove();
      microphoneMuteListenerRegistration = null;
    }
    super.onDetach(detachEvent);
  }

  private void applyAudioState(AudioSlice audioSlice) {
    var outputs = audioSlice.outputDevices();
    var deviceArray = outputs.toArray(new AudioSlice.AudioDevice[0]);
    primaryOutputSelect.setItems(deviceArray);
    secondaryOutputSelect.setItems(deviceArray);

    var primarySelection =
        outputs.stream()
            .filter(device -> Objects.equals(device.id(), audioSlice.primaryOutputDeviceId()))
            .findFirst()
            .orElse(null);
    if (primarySelection != null) {
      primaryOutputSelect.setValue(primarySelection);
    } else {
      primaryOutputSelect.clear();
    }
    primaryOutputSelect.setEnabled(!outputs.isEmpty());

    var secondarySelection =
        outputs.stream()
            .filter(device -> Objects.equals(device.id(), audioSlice.secondaryOutputDeviceId()))
            .findFirst()
            .orElse(null);
    if (secondarySelection != null) {
      secondaryOutputSelect.setValue(secondarySelection);
    } else {
      secondaryOutputSelect.clear();
    }
    secondaryOutputSelect.setEnabled(!outputs.isEmpty());

    testPrimaryButton.setEnabled(primarySelection != null);
    testSecondaryButton.setEnabled(secondarySelection != null);

    var inputs = audioSlice.inputDevices();
    var inputArray = inputs.toArray(new AudioSlice.AudioDevice[0]);
    microphoneSelect.setItems(inputArray);
    var microphoneSelection =
        inputs.stream()
            .filter(device -> Objects.equals(device.id(), audioSlice.selectedInputDeviceId()))
            .findFirst()
            .orElse(null);
    if (microphoneSelection != null) {
      microphoneSelect.setValue(microphoneSelection);
    } else {
      microphoneSelect.clear();
    }
    updateMicTestControls();
  }

  private void playPrimaryTestSound() {
    var selected = primaryOutputSelect.getValue();
    audioDeviceAdapter.playTestSoundOn(selected != null ? selected.id() : null);
  }

  private void playSecondaryTestSound() {
    var selected = secondaryOutputSelect.getValue();
    if (selected != null) {
      audioDeviceAdapter.playTestSoundOn(selected.id());
    }
  }

  private void toggleMicTestRecording() {
    if (micTestRecording) {
      audioDeviceAdapter.stopMicTest();
    } else if (microphoneSelect.getValue() != null) {
      audioDeviceAdapter.startMicTest();
    }
  }

  private void updateMicTestControls() {
    var hasMicrophone = microphoneSelect.getValue() != null;
    micTestButton.setEnabled(hasMicrophone && !microphoneMuted);
    var buttonLabelKey =
        micTestRecording
            ? "terminal.settings.audio.microphone.test.stop"
            : "terminal.settings.audio.microphone.test.start";
    micTestButton.setText(getTranslation(buttonLabelKey));
    micTestIndicator.setVisible(micTestRecording);
    micTestIndicatorText.setText(
        getTranslation("terminal.settings.audio.microphone.test.recording"));
  }

  private void toggleMicrophoneMute() {
    audioDeviceAdapter.muteMicrophone(!microphoneMuted);
  }

  private void updateMicrophoneMuteButton() {
    var key =
        microphoneMuted
            ? "terminal.settings.audio.microphone.unmute"
            : "terminal.settings.audio.microphone.mute";
    microphoneMuteButton.setText(getTranslation(key));
  }
}
