package com.matejik.terminal.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableSupplier;

public record TerminalNavItem(
    String translationKey,
    SerializableSupplier<Component> icon,
    Class<? extends Component> navigationTarget) {}
