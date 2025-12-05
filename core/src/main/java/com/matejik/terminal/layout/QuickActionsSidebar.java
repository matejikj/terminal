package com.matejik.terminal.layout;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public final class QuickActionsSidebar extends Composite<Div> {

    public QuickActionsSidebar() {
        var root = getContent();
        root.addClassNames("terminal-right-sidebar", LumoUtility.Padding.SMALL);
        var container = new VerticalLayout();
        container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL,
                LumoUtility.Margin.NONE);
        container.setPadding(false);

        container.add(createPrimaryAction(VaadinIcon.VOLUME_OFF, "terminal.actions.mute"),
                createPrimaryAction(VaadinIcon.SUN_O, "terminal.actions.theme"),
                createPrimaryAction(VaadinIcon.BELL, "terminal.actions.notifications"));

        root.add(container);
    }

    private Button createPrimaryAction(VaadinIcon icon, String translationKey) {
        var button = new Button(icon.create(), event -> {
        });
        button.setTooltipText(getTranslation(translationKey));
        button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        button.addClassNames("quick-action");
        return button;
    }
}
