package com.matejik.terminal.layout;

import com.matejik.sip.SipCall;
import com.matejik.sip.SipCallState;
import com.matejik.sip.SipSessionState;
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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

public final class ActiveCallsPanel extends Composite<Div> {

    private final SipTerminalService sipService;
    private Registration sipRegistration;
    private final VerticalLayout callList = new VerticalLayout();
    private final Span selectedRemote = new Span();
    private final Span selectedState = new Span();
    private final Span selectedDuration = new Span();
    private final Div callAvatar = new Div();
    private final Button answerButton;
    private final Button hangupButton;
    private final Div actionsGrid = new Div();
    private SipCall selectedCall;

    public ActiveCallsPanel(SipTerminalService sipService) {
        this.sipService = sipService;
        var container = getContent();
        container.addClassNames("terminal-right-panel", LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX,
            LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM);

        var listCard = new Div();
        listCard.addClassNames("call-card", "call-card--list");
        var listTitle = new H5(getTranslation("terminal.calls.title"));
        listTitle.addClassNames(LumoUtility.Margin.NONE);

        callList.addClassNames("call-list", LumoUtility.Gap.SMALL, LumoUtility.Padding.NONE,
            LumoUtility.Margin.NONE);
        callList.setPadding(false);
        callList.setSpacing(false);

        listCard.add(listTitle, callList);

        var detailCard = new Div();
        detailCard.addClassNames("call-card", "call-card--detail");

        callAvatar.addClassNames("call-detail__avatar");
        callAvatar.setText("----");

        selectedRemote.addClassNames("call-detail__identity");
        selectedState.addClassNames("call-detail__meta");
        selectedDuration.addClassNames("call-detail__duration");

        var detailText = new Div(selectedRemote, selectedState, selectedDuration);
        detailText.addClassNames("call-detail__text", LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
            LumoUtility.Gap.XSMALL, LumoUtility.AlignItems.CENTER);

        detailCard.add(callAvatar, detailText);

        var actionsCard = new Div();
        actionsCard.addClassNames("call-card", "call-card--actions");
        actionsGrid.addClassNames("call-actions-grid");

        answerButton = createActionButton(VaadinIcon.PHONE, () -> Optional.ofNullable(selectedCall)
            .ifPresent(sipService::answer));
        answerButton.addClassNames("call-action-button", "call-action-button--primary");

        hangupButton = createActionButton(VaadinIcon.CLOSE_CIRCLE, () -> Optional.ofNullable(selectedCall)
            .ifPresent(sipService::hangUp));
        hangupButton.addClassNames("call-action-button", "call-action-button--danger");

        var holdButton = createActionButton(VaadinIcon.PAUSE, this::showPlaceholderAction);
        var transferButton = createActionButton(VaadinIcon.EXIT_O, this::showPlaceholderAction);
        var muteButton = createActionButton(VaadinIcon.VOLUME_OFF, this::showPlaceholderAction);
        var recordButton = createActionButton(VaadinIcon.CIRCLE, this::showPlaceholderAction);
        var notesButton = createActionButton(VaadinIcon.CLIPBOARD_TEXT, this::showPlaceholderAction);
        var keypadButton = createActionButton(VaadinIcon.GRID_SMALL, this::showPlaceholderAction);

        actionsGrid.add(answerButton, holdButton, transferButton, muteButton,
            recordButton, notesButton, keypadButton, hangupButton);
        actionsCard.add(actionsGrid);

        container.add(listCard, detailCard, actionsCard);
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
        wrapper.addClassNames("terminal-call-row", "call-row" + stateTheme(call));

        var identity = new Div(new Span(call.remoteAddress()), new Span(getTranslation(
                "terminal.calls.state." + call.state().name().toLowerCase())));
        identity.addClassNames("call-row__identity", LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.XSMALL);

        var focus = new Button(VaadinIcon.CHEVRON_RIGHT.create(), event -> {
            selectedCall = call;
            updateHighlightedCall();
        });
        focus.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        focus.addClassNames("call-row__focus");

        wrapper.add(identity, focus);
        return wrapper;
    }

    private void updateHighlightedCall() {
        if (selectedCall == null) {
            selectedRemote.setText(getTranslation("terminal.calls.none"));
            selectedState.setText("");
            selectedDuration.setText("");
            callAvatar.setText("");
            answerButton.setEnabled(false);
            hangupButton.setEnabled(false);
            return;
        }
        selectedRemote.setText(selectedCall.remoteAddress());
        selectedState.setText(getTranslation("terminal.calls.state." + selectedCall.state().name().toLowerCase()));
        selectedDuration.setText(formatDuration(selectedCall));
        callAvatar.setText(buildAvatarText(selectedCall));
        answerButton.setEnabled(selectedCall.state() == SipCallState.RINGING || selectedCall.state() == SipCallState.CONNECTING);
        hangupButton.setEnabled(true);
    }

    private String formatDuration(SipCall call) {
        var startedAt = Optional.ofNullable(call.startedAt()).orElse(Instant.now());
        var duration = Duration.between(startedAt, Instant.now());
        var minutes = duration.toMinutesPart();
        var seconds = duration.toSecondsPart();
        return minutes + ":" + String.format("%02d", seconds);
    }

    private String buildAvatarText(SipCall call) {
        var remote = Optional.ofNullable(call.remoteAddress()).orElse("").trim();
        if (remote.isEmpty()) {
            return "CALL";
        }
        return remote.substring(0, Math.min(4, remote.length())).toUpperCase(Locale.ROOT);
    }

    private Button createActionButton(VaadinIcon icon, Runnable action) {
        var button = new Button(icon.create(), event -> action.run());
        button.addClassNames("call-action-button");
        button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    private String stateTheme(SipCall call) {
        return switch (call.state()) {
            case IN_CALL -> " call-row--success";
            case RINGING, CONNECTING -> " call-row--info";
            default -> " call-row--idle";
        };
    }

    private void showPlaceholderAction() {
        Notification.show("Funkce bude dostupná později");
    }
}
