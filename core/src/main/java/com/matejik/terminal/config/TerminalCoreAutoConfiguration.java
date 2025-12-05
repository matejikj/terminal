package com.matejik.terminal.config;

import com.matejik.sip.MockSipClient;
import com.matejik.sip.SipClient;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ComponentScan(basePackages = "com.matejik.terminal")
@EnableConfigurationProperties(TerminalProperties.class)
public class TerminalCoreAutoConfiguration {

    @Bean
    RestClient terminalRestClient(RestClient.Builder builder, TerminalProperties properties) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .baseUrl(properties.api().getBaseUrl().toString())
                .build();
    }

    @Bean
    I18NProvider terminalI18NProvider(TerminalProperties properties) {
        return new TerminalI18NProvider(properties.ui().getDefaultLocale());
    }

    @Bean
    @VaadinSessionScope
    SipClient sipClient(TerminalProperties properties) {
        // TODO: inject native client factory when the JNI bindings are wired
        if (properties.sip().getMode() == TerminalProperties.SipBackendMode.NATIVE) {
            throw new IllegalStateException("Native SIP backend not yet wired");
        }
        return new MockSipClient();
    }
}
