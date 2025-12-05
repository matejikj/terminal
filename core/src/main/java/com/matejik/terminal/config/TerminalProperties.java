package com.matejik.terminal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;

@ConfigurationProperties(prefix = "terminal")
public class TerminalProperties {

    private final ApiProperties api = new ApiProperties();
    private final SipProperties sip = new SipProperties();
    private final UiProperties ui = new UiProperties();

    public ApiProperties api() {
        return api;
    }

    public SipProperties sip() {
        return sip;
    }

    public UiProperties ui() {
        return ui;
    }

    public static final class ApiProperties {
        /**
         * Base URL of the domain backend from which the Vaadin application reads data.
         */
        private URI baseUrl = URI.create("http://localhost:8081");
        /**
         * TTL for cached collection data inside the {@link com.matejik.terminal.data.CollectionService}.
         */
        private Duration cacheTtl = Duration.ofSeconds(30);

        public URI getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Duration getCacheTtl() {
            return cacheTtl;
        }

        public void setCacheTtl(Duration cacheTtl) {
            this.cacheTtl = cacheTtl;
        }
    }

    public enum SipBackendMode {
        MOCK,
        NATIVE
    }

    public static final class SipProperties {

        private SipBackendMode mode = SipBackendMode.MOCK;
        private boolean autoConnect = false;

        public SipBackendMode getMode() {
            return mode;
        }

        public void setMode(SipBackendMode mode) {
            this.mode = mode;
        }

        public boolean isAutoConnect() {
            return autoConnect;
        }

        public void setAutoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
        }
    }

    public static final class UiProperties {
        private Locale defaultLocale = Locale.ENGLISH;

        public Locale getDefaultLocale() {
            return defaultLocale;
        }

        public void setDefaultLocale(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
        }
    }
}
