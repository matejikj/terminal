# Terminal README

Terminal is now split into three Maven modules so that you can ship two customer-specific variants on top of a shared Vaadin/Spring Boot core:

- **core** – contains all shared Vaadin views, services, data access, resources, and tests that used to live under `src`.
- **product** – brings in the `core` module and contains brand-specific bootstrapping/resources for the Product customer.
- **skoda** – same idea as `product`, but with Skoda-specific overrides.

## Project Structure

```
.
├── core
│   └── src
│       ├── main/java/com/matejik/...
│       ├── main/frontend
│       └── test/java/com/matejik/...
├── product
│   └── src
│       ├── main/java/com/matejik/product/ProductApplication.java
│       └── main/resources/application-product.properties
└── skoda
    └── src
        ├── main/java/com/matejik/skoda/SkodaApplication.java
        └── main/resources/application-skoda.properties
```

`Application.java` (in the `core` module) is still the main Spring Boot/Vaadin configuration class. The two brand modules simply start that configuration with their own active profile so they can override beans, themes, or properties as needed.

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
