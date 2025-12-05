package com.matejik.terminal.state;

import java.util.Objects;

/**
 * Type-safe key that identifies a value stored inside the {@link StateStore}.
 *
 * @param <T> type of the value.
 */
public final class StateKey<T> {

    private final String name;
    private final Class<T> type;

    private StateKey(String name, Class<T> type) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
    }

    public static <T> StateKey<T> of(String name, Class<T> type) {
        return new StateKey<>(name, type);
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    @Override
    public String toString() {
        return "StateKey[" + name + ':' + type.getSimpleName() + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StateKey<?> stateKey = (StateKey<?>) o;
        return name.equals(stateKey.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
