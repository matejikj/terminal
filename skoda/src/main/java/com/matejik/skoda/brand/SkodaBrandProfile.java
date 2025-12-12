package com.matejik.skoda.brand;

import com.matejik.terminal.brand.BrandProfile;
import com.vaadin.flow.component.html.Span;
import org.springframework.stereotype.Component;

@Component
public class SkodaBrandProfile implements BrandProfile {

  @Override
  public String name() {
    return "Škoda Terminal";
  }

  @Override
  public com.vaadin.flow.component.Component logo() {
    var span = new Span("ŠT");
    span.getStyle().set("font-weight", "bold");
    return span;
  }
}
