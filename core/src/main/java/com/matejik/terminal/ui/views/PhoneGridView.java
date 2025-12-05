package com.matejik.terminal.ui.views;

import com.matejik.sip.SipCall;
import com.matejik.sip.SipCallState;
import com.matejik.sip.SipSessionState;
import com.matejik.terminal.sip.SipTerminalService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.ArrayList;
import java.util.List;

public class PhoneGridView extends Composite<Div> {

    private final SipTerminalService sipTerminalService;
    private Registration sipRegistration;
    private final TextField dialInput = new TextField();
    private final Button dialButton = new Button();
    private final List<TerminalTile> tiles = new ArrayList<>();
    private final String tileReadyText;

    public PhoneGridView(SipTerminalService sipTerminalService) {
        this.sipTerminalService = sipTerminalService;
        this.tileReadyText = resolveReadyLabel();
        var root = getContent();
        root.addClassNames("terminal-dashboard", LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM);

        root.add(buildPrimaryTabs(), buildSecondaryTabs(), buildGridPanel());
    }

    private Div buildPrimaryTabs() {
        var tabs = new Div();
        tabs.addClassNames("terminal-primary-tabs");
        List<TabSpec> specs = List.of(
                new TabSpec("HDP_Emergo_tab1", false, TabStyle.PRIMARY),
                new TabSpec("HDP_Obchodní_tab1", true, TabStyle.PRIMARY),
                new TabSpec("HDP_Emergo_tab2", false, TabStyle.PRIMARY),
                new TabSpec("HDP_Test", false, TabStyle.PRIMARY)
        );
        specs.forEach(spec -> tabs.add(createTab(spec)));
        return tabs;
    }

    private Div buildSecondaryTabs() {
        var tabs = new Div();
        tabs.addClassNames("terminal-secondary-tabs");
        List<TabSpec> specs = List.of(
                new TabSpec("Elektrárny", true, TabStyle.SECONDARY),
                new TabSpec("Rozvody", false, TabStyle.SECONDARY),
                new TabSpec("Distribuce", false, TabStyle.SECONDARY),
                new TabSpec("Testovací", false, TabStyle.SECONDARY)
        );
        specs.forEach(spec -> tabs.add(createTab(spec)));
        var divider = new Div();
        divider.addClassNames("terminal-grid-divider");
        var wrapper = new Div(tabs, divider);
        wrapper.addClassNames("terminal-secondary-stack");
        return wrapper;
    }

    private Div buildGridPanel() {
        var panel = new Div();
        panel.addClassNames("terminal-glass-panel");

        var accent = new Div();
        accent.addClassNames("terminal-panel-accent");

        var grid = new Div();
        grid.addClassNames("terminal-tile-grid");
        TILE_BLUEPRINTS.forEach(blueprint -> {
            var tile = new TerminalTile(blueprint);
            tiles.add(tile);
            grid.add(tile);
        });

        panel.add(accent, grid, buildDialer());
        return panel;
    }

    private Div buildDialer() {
        var dialer = new Div();
        dialer.addClassNames("terminal-dialer", LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL,
                LumoUtility.AlignItems.CENTER);

        dialInput.setPlaceholder("+420 123 456 789");
        dialInput.setPrefixComponent(VaadinIcon.PHONE.create());
        dialInput.addClassNames("terminal-dialer__field");

        dialButton.setText(getTranslation("terminal.phone.call"));
        dialButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialButton.addClassNames("terminal-dialer__button");
        dialButton.addClickListener(event -> {
            var target = dialInput.getValue();
            if (target != null && !target.isBlank()) {
                sipTerminalService.dial(target);
                dialInput.clear();
            }
        });

        dialer.add(dialInput, dialButton);
        return dialer;
    }

    private Div createTab(TabSpec spec) {
        var label = new Span(spec.label());
        label.addClassNames("terminal-tab__label");
        var tab = new Div(label);
        tab.addClassNames("terminal-tab", spec.style().className());
        if (spec.active()) {
            tab.addClassName("is-active");
        }
        return tab;
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
        tiles.forEach(TerminalTile::reset);
        int index = 0;
        for (var call : state.activeCalls()) {
            if (index >= tiles.size()) {
                break;
            }
            tiles.get(index).showCall(call);
            index++;
        }
    }

    private record TabSpec(String label, boolean active, TabStyle style) {
    }

    private enum TabStyle {
        PRIMARY("terminal-tab--primary"),
        SECONDARY("terminal-tab--secondary");

        private final String className;

        TabStyle(String className) {
            this.className = className;
        }

        public String className() {
            return className;
        }
    }

    private static final List<TileBlueprint> TILE_BLUEPRINTS = List.of(
            new TileBlueprint("eti1", "ETI1", TileVariant.OLIVE, false),
            new TileBlueprint("eti2", "ETI2", TileVariant.PURPLE, false),
            new TileBlueprint("kralupy", "Kralupy", TileVariant.BLUE, false),
            new TileBlueprint("melnik", "Mělník", TileVariant.TEAL, false),
            new TileBlueprint("hdp", "HDP_ETI", TileVariant.RED, false),
            new TileBlueprint("tusimice", "Tušimice", TileVariant.OLIVE, false),
            new TileBlueprint("disabled-0", "", TileVariant.DISABLED, true),
            new TileBlueprint("prunerov", "Prunéřov", TileVariant.OLIVE, false),
            new TileBlueprint("pocerady", "Počerady", TileVariant.PURPLE, false),
            new TileBlueprint("chvaletice", "Chvaletice", TileVariant.BLUE, false),
            new TileBlueprint("detmarovice", "Dětmarovice", TileVariant.TEAL, false),
            new TileBlueprint("ledvice", "Ledvice", TileVariant.BLUE, false),
            new TileBlueprint("mestoa", "Město A", TileVariant.OLIVE, false),
            new TileBlueprint("disabled-1", "", TileVariant.DISABLED, true),
            new TileBlueprint("stanice1", "Stanice 1", TileVariant.TEAL, false),
            new TileBlueprint("stanice2", "Stanice 2", TileVariant.BLUE, false),
            new TileBlueprint("stanice3", "Stanice 3", TileVariant.PURPLE, false),
            new TileBlueprint("stanice4", "Stanice 4", TileVariant.OLIVE, false),
            new TileBlueprint("stanice5", "Stanice 5", TileVariant.BLUE, false),
            new TileBlueprint("stanice6", "Stanice 6", TileVariant.TEAL, false),
            new TileBlueprint("disabled-2", "", TileVariant.DISABLED, true),
            new TileBlueprint("linka101", "Linka 101", TileVariant.OLIVE, false),
            new TileBlueprint("disabled-3", "", TileVariant.DISABLED, true),
            new TileBlueprint("linka102", "Linka 102", TileVariant.BLUE, false),
            new TileBlueprint("disabled-4", "", TileVariant.DISABLED, true),
            new TileBlueprint("linka103", "Linka 103", TileVariant.TEAL, false),
            new TileBlueprint("disabled-5", "", TileVariant.DISABLED, true),
            new TileBlueprint("disabled-6", "", TileVariant.DISABLED, true)
    );

    private enum TileVariant {
        OLIVE,
        PURPLE,
        BLUE,
        TEAL,
        RED,
        DISABLED
    }

    private record TileBlueprint(String id, String label, TileVariant variant, boolean disabled) {
    }

    private final class TerminalTile extends Div {
        private final TileBlueprint blueprint;
        private final Span label = new Span();
        private final Span state = new Span();
        private final Icon icon = VaadinIcon.PHONE.create();

        private TerminalTile(TileBlueprint blueprint) {
            this.blueprint = blueprint;
            addClassNames("terminal-tile", "terminal-tile--" + blueprint.variant().name().toLowerCase());
            if (blueprint.disabled()) {
                addClassName("terminal-tile--disabled");
            }

            icon.addClassNames("terminal-tile__icon");

            label.addClassNames("terminal-tile__code");
            label.setText(blueprint.label());
            state.addClassNames("terminal-tile__hint");
            state.setText(tileReadyText);

            var content = new Div(label, state);
            content.addClassNames("terminal-tile__body");

            add(content, icon);
        }

        private void reset() {
            state.setText(tileReadyText);
            removeClassName("terminal-tile--alert");
            removeClassName("terminal-tile--success");
        }

        private void showCall(SipCall call) {
            if (blueprint.disabled()) {
                return;
            }
            var stateClass = switch (call.state()) {
                case IN_CALL -> "terminal-tile--success";
                case RINGING, CONNECTING -> "terminal-tile--alert";
                default -> "";
            };
            if (!stateClass.isBlank()) {
                addClassName(stateClass);
            }
            state.setText(call.remoteAddress());
        }
    }

    private String resolveReadyLabel() {
        var translation = getTranslation("terminal.tile.status.ready");
        if (translation == null || translation.equals("terminal.tile.status.ready")) {
            return "Připraveno";
        }
        return translation;
    }
}
