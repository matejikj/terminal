package com.matejik.terminal.data;

import java.time.Instant;
import java.util.List;

public record CollectionState<T>(List<T> items, Instant lastFetched) {

    public static <T> CollectionState<T> empty() {
        return new CollectionState<>(List.of(), Instant.EPOCH);
    }
}
