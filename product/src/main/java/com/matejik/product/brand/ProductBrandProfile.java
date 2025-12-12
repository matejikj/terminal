package com.matejik.product.brand;

import com.matejik.terminal.application.brand.BrandProfile;
import com.vaadin.flow.component.html.Span;
import org.springframework.stereotype.Component;

@Component
public class ProductBrandProfile implements BrandProfile {

  @Override
  public String name() {
    return "Terminal Product";
  }

  @Override
  public com.vaadin.flow.component.Component logo() {
    return new Span("TP");
  }
}
