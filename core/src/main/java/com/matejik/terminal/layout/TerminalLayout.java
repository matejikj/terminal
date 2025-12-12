package com.matejik.terminal.layout;

import com.matejik.terminal.application.command.CallCommandService;
import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.brand.BrandProfile;
import com.matejik.terminal.i18n.TerminalLocaleService;
import com.matejik.terminal.infrastructure.audio.AudioDeviceAdapter;
import com.matejik.terminal.navigation.TerminalNavigationRegistry;
import com.matejik.terminal.ui.components.AudioClientBridge;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.Push;

@CssImport("./themes/terminal/styles.css")
@Push
public class TerminalLayout extends AppLayout {

  private final Div viewContainer = new Div();

  public TerminalLayout(
      BrandProfile brandProfile,
      TerminalNavigationRegistry navigationRegistry,
      CallCommandService callCommandService,
      AppStore appStore,
      TerminalLocaleService localeService,
      AudioDeviceAdapter audioDeviceAdapter) {
    setPrimarySection(Section.NAVBAR);
    getElement().getStyle().set("height", "100%");
    getElement().getStyle().set("width", "100%");
    getElement().getStyle().set("overflow", "hidden");

    var stage = new Div();
    stage.addClassNames("terminal-stage");

    var statusBar = new TerminalStatusBar(brandProfile, appStore, localeService);

    var shell = new Div();
    shell.addClassNames("terminal-shell");

    shell.add(new TerminalMenu(navigationRegistry, localeService));

    viewContainer.addClassNames("terminal-main-content");
    var main = new Div(viewContainer);
    main.addClassNames("terminal-main");
    shell.add(main);

    shell.add(new ActiveCallsPanel(appStore, callCommandService));
    shell.add(new QuickActionsSidebar());
    shell.add(new AudioClientBridge(audioDeviceAdapter));

    stage.add(statusBar, shell);
    setContent(stage);
  }

  @Override
  public void showRouterLayoutContent(HasElement content) {
    viewContainer.getElement().removeAllChildren();
    if (content != null) {
      viewContainer.getElement().appendChild(content.getElement());
    }
  }
}
