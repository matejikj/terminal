package com.matejik.terminal.application.brand;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

public interface BrandProfile {

  String name();

  default Component logo() {
    return VaadinIcon.PHONE.create();
  }

  default String accentColor() {
    return "var(--lumo-primary-color)";
  }

  default String supportEmail() {
    return "support@example.com";
  }
}
