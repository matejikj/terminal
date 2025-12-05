package com.matejik.terminal.state;

import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@Component
@VaadinSessionScope
public class StateStore {

    private final Map<StateKey<?>, Object> values = new ConcurrentHashMap<>();
    private final Map<StateKey<?>, CopyOnWriteArrayList<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T> T get(StateKey<T> key) {
        return key.type().cast(values.get(key));
    }

    public <T> void set(StateKey<T> key, T value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
        notifyListeners(key, value);
    }

    public <T> void clear(StateKey<T> key) {
        values.remove(key);
        notifyListeners(key, null);
    }

    public <T> void update(StateKey<T> key, UnaryOperator<T> updater) {
        var updated = values.compute(key, (k, current) -> updater.apply(key.type().cast(current)));
        notifyListeners(key, key.type().cast(updated));
    }

    public <T> Registration addListener(StateKey<T> key, Consumer<T> listener) {
        listeners.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> listeners.computeIfPresent(key, (ignored, list) -> {
            list.remove(listener);
            return list.isEmpty() ? null : list;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> void notifyListeners(StateKey<T> key, T value) {
        var list = listeners.get(key);
        if (list == null) {
            return;
        }
        list.forEach(listener -> ((Consumer<T>) listener).accept(value));
    }
}
