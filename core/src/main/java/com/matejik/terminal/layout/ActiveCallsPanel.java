package com.matejik.terminal.layout;

import com.matejik.terminal.sip.SipCall;
import com.matejik.terminal.sip.SipCallState;
import com.matejik.terminal.sip.SipSessionState;
import com.matejik.terminal.sip.SipTerminalService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Comparator;
import java.util.Optional;

public final class ActiveCallsPanel extends Composite<Div> {

    private final SipTerminalService sipService;
    private Registration sipRegistration;
    private final VerticalLayout callList = new VerticalLayout();
    private final Span selectedRemote = new Span();
    private final Span selectedState = new Span();
    private final ProgressBar callProgress = new ProgressBar();
    private final Button answerButton;
    private final Button hangupButton;
    private SipCall selectedCall;

    public ActiveCallsPanel(SipTerminalService sipService) {
        this.sipService = sipService;
        var container = getContent();
        container.addClassNames("terminal-right-panel", LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM);

        var title = new H5(getTranslation("terminal.calls.title"));
        title.addClassNames(LumoUtility.Margin.NONE);

        callList.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
        callList.setPadding(false);
        callList.setSpacing(false);

        var activeCall = new Div(selectedRemote, selectedState, callProgress);
        activeCall.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.XSMALL);
        callProgress.setIndeterminate(true);

        answerButton = new Button(getTranslation("terminal.calls.answer"), event -> Optional.ofNullable(selectedCall)
                .ifPresent(sipService::answer));
        answerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        hangupButton = new Button(getTranslation("terminal.calls.hangup"), event -> Optional.ofNullable(selectedCall)
                .ifPresent(sipService::hangUp));
        hangupButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        var controls = new Div(answerButton, hangupButton);
        controls.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL, LumoUtility.FlexWrap.WRAP);

        container.add(title, callList, activeCall, controls);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        sipRegistration = sipService.observeSessionState(state ->
                attachEvent.getUI().access(() -> updateState(state)));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (sipRegistration != null) {
            sipRegistration.remove();
            sipRegistration = null;
        }
    }

    private void updateState(SipSessionState state) {
        callList.removeAll();
        state.activeCalls().stream()
                .sorted(Comparator.comparing(SipCall::startedAt))
                .forEach(call -> callList.add(createCallRow(call)));
        selectedCall = pickHighlightedCall(state);
        updateHighlightedCall();
    }

    private SipCall pickHighlightedCall(SipSessionState state) {
        if (selectedCall != null) {
            var stillActive = state.activeCalls().stream()
                    .filter(call -> call.id().equals(selectedCall.id()))
                    .findFirst();
            if (stillActive.isPresent()) {
                return stillActive.get();
            }
        }
        return state.activeCalls().stream()
                .filter(call -> call.state() == SipCallState.IN_CALL)
                .findFirst()
                .orElseGet(() -> state.activeCalls().stream().findFirst().orElse(null));
    }

    private Component createCallRow(SipCall call) {
        var wrapper = new Div();
        wrapper.addClassNames("call-row", LumoUtility.Display.FLEX, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.AlignItems.CENTER, LumoUtility.Padding.SMALL, LumoUtility.BorderRadius.SMALL,
                "terminal-call-row");
        var identity = new Div(new Span(call.remoteAddress()), new Span(getTranslation(
                "terminal.calls.state." + call.state().name().toLowerCase())));
        identity.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        var selectButton = new Button(getTranslation("terminal.calls.focus"), event -> {
            selectedCall = call;
            updateHighlightedCall();
        });
        selectButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        wrapper.add(identity, selectButton);
        return wrapper;
    }

    private void updateHighlightedCall() {
        if (selectedCall == null) {
            selectedRemote.setText(getTranslation("terminal.calls.none"));
            selectedState.setText("");
            answerButton.setEnabled(false);
            hangupButton.setEnabled(false);
            callProgress.setVisible(false);
            return;
        }
        selectedRemote.setText(selectedCall.remoteAddress());
        selectedState.setText(getTranslation("terminal.calls.state." + selectedCall.state().name().toLowerCase()));
        answerButton.setEnabled(selectedCall.state() == SipCallState.RINGING || selectedCall.state() == SipCallState.CONNECTING);
        hangupButton.setEnabled(true);
        callProgress.setVisible(true);
        callProgress.setIndeterminate(selectedCall.state() != SipCallState.IN_CALL);
    }
}
