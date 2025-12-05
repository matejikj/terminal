package com.matejik.terminal.sip;

import com.matejik.sip.*;
import com.matejik.terminal.state.StateStore;
import com.matejik.terminal.state.TerminalStateKeys;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
@VaadinSessionScope
public class SipTerminalService {

    private final SipClient sipClient;
    private final StateStore stateStore;
    private final SipRegistration clientRegistration;

    public SipTerminalService(SipClient sipClient, StateStore stateStore) {
        this.sipClient = sipClient;
        this.stateStore = stateStore;
        this.stateStore.set(TerminalStateKeys.SIP_SESSION, SipSessionState.initial());
        this.clientRegistration = sipClient.addListener(this::handleEvent);
    }

    public SipSessionState currentState() {
        return Optional.ofNullable(stateStore.get(TerminalStateKeys.SIP_SESSION))
                .orElseGet(SipSessionState::initial);
    }

    public Registration observeSessionState(Consumer<SipSessionState> listener) {
        listener.accept(currentState());
        return stateStore.addListener(TerminalStateKeys.SIP_SESSION, listener);
    }

    public void connect(SipAccountCredentials credentials) {
        updateState(state -> state.withConnection(SipConnectionStatus.CONNECTING, Optional.of(credentials)));
        sipClient.connect(credentials);
    }

    public void disconnect() {
        sipClient.disconnect();
        updateState(SipSessionState::withoutAccount);
    }

    public SipCall dial(String destination) {
        var call = sipClient.makeCall(destination);
        updateState(state -> state.withCallUpdated(call));
        return call;
    }

    public void answer(SipCall call) {
        sipClient.answerCall(call);
    }

    public void hangUp(SipCall call) {
        sipClient.hangupCall(call);
    }

    private void handleEvent(SipEvent event) {
        updateState(state -> switch (event) {
            case SipEvent.ConnectionChanged connectionChanged ->
                    state.withConnection(connectionChanged.status(), connectionChanged.account());
            case SipEvent.CallProgress callProgress -> state.withCallUpdated(callProgress.call());
            case SipEvent.CallTerminated callTerminated ->
                    state.withCallTerminated(callTerminated.call().id(), callTerminated.call());
        });
    }

    private void updateState(java.util.function.UnaryOperator<SipSessionState> updater) {
        stateStore.update(TerminalStateKeys.SIP_SESSION, state -> {
            if (state == null) {
                return updater.apply(SipSessionState.initial());
            }
            return updater.apply(state);
        });
    }

    @PreDestroy
    void dispose() {
        clientRegistration.remove();
    }
}
