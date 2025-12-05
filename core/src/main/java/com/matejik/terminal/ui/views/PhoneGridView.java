package com.matejik.terminal.ui.views;

import com.matejik.sip.SipCall;
import com.matejik.sip.SipSessionState;
import com.matejik.terminal.sip.SipTerminalService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.ArrayList;

public class PhoneGridView extends Composite<Div> {

    private final SipTerminalService sipTerminalService;
    private final ListDataProvider<SipCall> callProvider = new ListDataProvider<>(new ArrayList<>());
    private Registration sipRegistration;
    private final TextField dialInput = new TextField();

    public PhoneGridView(SipTerminalService sipTerminalService) {
        this.sipTerminalService = sipTerminalService;
        var root = getContent();
        root.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM);

        dialInput.setLabel(getTranslation("terminal.phone.number"));
        dialInput.setPlaceholder("+420 123 456 789");
        dialInput.setPrefixComponent(VaadinIcon.PHONE.create());

        var dialButton = new Button(getTranslation("terminal.phone.call"), event -> {
            var target = dialInput.getValue();
            if (target != null && !target.isBlank()) {
                sipTerminalService.dial(target);
                dialInput.clear();
            }
        });
        dialButton.setDisableOnClick(true);
        dialButton.addClickListener(event -> dialButton.setEnabled(true));

        var dialer = new HorizontalLayout(dialInput, dialButton);
        dialer.setWidthFull();
        dialer.setAlignItems(FlexComponent.Alignment.END);

        var callGrid = new Grid<SipCall>();
        callGrid.addColumn(SipCall::remoteAddress).setHeader(getTranslation("terminal.phone.remote"));
        callGrid.addColumn(call -> call.direction().name()).setHeader(getTranslation("terminal.phone.direction"));
        callGrid.addColumn(call -> call.state().name()).setHeader(getTranslation("terminal.phone.state"));
        callGrid.setItems(callProvider);
        callGrid.setAllRowsVisible(true);

        root.add(dialer, callGrid);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        sipRegistration = sipTerminalService.observeSessionState(state ->
                attachEvent.getUI().access(() -> applyState(state)));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (sipRegistration != null) {
            sipRegistration.remove();
            sipRegistration = null;
        }
    }

    private void applyState(SipSessionState state) {
        callProvider.getItems().clear();
        callProvider.getItems().addAll(state.activeCalls());
        callProvider.refreshAll();
    }
}
