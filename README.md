# Terminal

## Running the Applications

From the project root you can use the Maven wrapper shortcuts:

```bash
./mvnw             # starts the Product dev server (-pl product -am spring-boot:run)
./mvnw product     # same as above, explicitly targeting Product
./mvnw skoda       # starts the Skoda dev server (-pl skoda -am spring-boot:run)
```

When you pass extra goals/flags after `product` or `skoda`, the wrapper will keep the module selection but use *your* goals. For example:

```bash
./mvnw product -Dspring-boot.run.profiles=dev spring-boot:run
./mvnw skoda clean package
```

## Hot Reload Workflow

Vaadin dev mode and Spring Boot DevTools are enabled in the shared `core` module. Keep the application running (for example `./mvnw -pl product -am -DskipTests spring-boot:run`) and let your IDE recompile classes automatically:

- **VS Code** – install [`entr`](https://eradman.com/entrproject/) once (`sudo apt install entr`). The repo ships with `.vscode/settings.json` and `.vscode/tasks.json` so that VS Code starts the `watch:maven-compile-product` task automatically when you open the folder. Allow the automatic task when prompted; it watches Java sources in `core`/`product` and runs `./mvnw -pl product -am -DskipTests compile` on every save so DevTools can hot-reload instantly.
- **IntelliJ IDEA** – the shared workspace settings already enable “Build project automatically” and “Allow auto-make to start even if developed application is running”. Just trust the project and start the `ProductApplication` or `SkodaApplication` Run Configuration; IntelliJ will emit new `.class` files after each save, which triggers DevTools without any manual Maven commands.

## Building for Production

Build both brand artifacts (core + Product + Skoda) with Vaadin's production profile in a single command:

```bash
./mvnw production          # equivalent to ./mvnw -Pproduction package
```

To build just one customer artifact, keep using the module shortcuts but add your preferred goals:

```bash
./mvnw product -Pproduction package
./mvnw skoda -Pproduction package
```

## Desktop Installers

`jpackage` is now wired straight into Maven so you can build installers per module without any helper scripts. Run the desired brand with the production profile and set `desktop.installer` to the target format:

- **Ubuntu / Debian (.deb)** – `./mvnw skoda -Pproduction -Ddesktop.installer=deb` (or swap `skoda` for `product`). Maven assembles the fat jar, feeds it to `jpackage`, and emits the `.deb` into `skoda/target/desktop`.
- **Windows (.exe)** – `./mvnw product -Pproduction -Ddesktop.installer=exe` when `jpackage` is available on `PATH` (OpenJDK 17+). The `.exe` installer lands in `product/target/desktop` with Start Menu/Desktop shortcuts pre-configured.

Each installer bundles the Vaadin theme assets, `audio-client-bridge.ts`, and logging setup so runtime logs land in `${USERPROFILE}/.terminal/logs/terminal.log` (Windows) or `${HOME}/.terminal/logs/terminal.log` (Linux). Launching the icon boots the embedded Vaadin server, auto-opens the default browser once it’s ready, and the **Settings → Application Control → “Shut down Terminal”** button cleanly stops it again.

## Installation & Launch

1. Build the installer for your target platform (see the previous section). The resulting package shows up under `<module>/target/desktop`.
2. **Ubuntu/Debian** – install the `.deb` either via GUI (double-click in your file manager) or CLI:  
   ```bash
   sudo dpkg -i product/target/desktop/terminal_*.deb
   sudo apt remove terminal
   ```  
   The installer creates menu + desktop entries named “Terminal”. Launch it from the desktop icon or via the application menu; it starts the embedded Vaadin server and opens your default browser automatically once the backend is ready.
3. **Windows** – run the generated `.exe`, follow the standard setup wizard, and leave the shortcut options enabled. After installation you can start “Terminal” from the Start Menu or desktop.
4. When the browser tab is no longer needed, return to the app and use **Settings → Application Control → “Shut down Terminal”** to stop the server cleanly. Logs always go to the per-user directory mentioned earlier so you can inspect failures after the app has been closed.

## Docker Image

Build a Docker image from the desired brand artifact, for example:

```bash
docker build -t terminal-product:latest -f Dockerfile .
```

If you use commercial Vaadin components, pass the license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Further Reading

The [Vaadin Getting Started guide](https://vaadin.com/docs/latest/getting-started) is still a great resource for understanding the shared `core` module and how to extend it with more features per customer.
