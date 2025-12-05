package com.matejik.terminal.state;

import com.matejik.terminal.sip.SipSessionState;

import java.util.Locale;

public final class TerminalStateKeys {

    private TerminalStateKeys() {
    }

    public static final StateKey<SipSessionState> SIP_SESSION = StateKey.of("sip.session", SipSessionState.class);
    public static final StateKey<Locale> ACTIVE_LOCALE = StateKey.of("app.locale", Locale.class);
}
