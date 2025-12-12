package com.matejik.terminal.domain.audio;

import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.audio.event.AudioDomainEvent;
import com.matejik.terminal.domain.audio.state.AudioAction;
import com.matejik.terminal.domain.audio.state.AudioSlice.AudioDevice;
import com.matejik.terminal.domain.audio.state.AudioSlice.AudioDeviceType;
import com.matejik.terminal.domain.audio.state.AudioSlice.AudioRoute;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class AudioStateProjectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AudioStateProjectionService.class);

  private final AppStore appStore;

  public AudioStateProjectionService(AppStore appStore) {
    this.appStore = appStore;
  }

  @EventListener
  public void onDeviceList(AudioDomainEvent.DeviceListChanged event) {
    handleDeviceList(event.inputs(), event.outputs());
  }

  @EventListener
  public void onClientDeviceList(AudioDomainEvent.ClientDeviceListChanged event) {
    handleDeviceList(event.inputs(), event.outputs());
  }

  @EventListener
  public void onDeviceSelected(AudioDomainEvent.DeviceSelected event) {
    dispatch(new AudioAction.SelectDevice(mapType(event.type()), event.deviceId()));
  }

  @EventListener
  public void onRouteChanged(AudioDomainEvent.RouteChanged event) {
    dispatch(new AudioAction.ChangeRoute(mapRoute(event.route())));
  }

  @EventListener
  public void onClientRouteChanged(AudioDomainEvent.ClientRouteChanged event) {
    dispatch(new AudioAction.ChangeRoute(mapRoute(event.route())));
  }

  @EventListener
  public void onMuteChanged(AudioDomainEvent.MuteChanged event) {
    dispatch(new AudioAction.UpdateMute(event.muted()));
  }

  @EventListener
  public void onClientMuteChanged(AudioDomainEvent.ClientMuteStateChanged event) {
    dispatch(new AudioAction.UpdateMute(event.muted()));
  }

  private void dispatch(AudioAction action) {
    appStore
        .dispatch(action)
        .exceptionally(
            error -> {
              LOGGER.error("Failed to project audio event {}", action, error);
              return null;
            });
  }

  private AudioDevice mapDevice(AudioDeviceSnapshot snapshot) {
    return new AudioDevice(snapshot.id(), snapshot.label(), mapType(snapshot.type()));
  }

  private void handleDeviceList(
      List<AudioDeviceSnapshot> inputs, List<AudioDeviceSnapshot> outputs) {
    List<AudioDevice> mappedInputs = inputs.stream().map(this::mapDevice).toList();
    List<AudioDevice> mappedOutputs = outputs.stream().map(this::mapDevice).toList();
    dispatch(new AudioAction.ReplaceDeviceLists(mappedInputs, mappedOutputs));
  }

  private AudioDeviceType mapType(com.matejik.terminal.domain.audio.AudioDeviceType type) {
    return switch (type) {
      case INPUT -> AudioDeviceType.INPUT;
      case OUTPUT -> AudioDeviceType.OUTPUT;
    };
  }

  private AudioRoute mapRoute(com.matejik.terminal.domain.audio.AudioRoute route) {
    return switch (route) {
      case SYSTEM_DEFAULT -> AudioRoute.SYSTEM_DEFAULT;
      case SPEAKER -> AudioRoute.SPEAKER;
      case HEADSET -> AudioRoute.HEADSET;
    };
  }
}
