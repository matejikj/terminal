package com.matejik.terminal.application.data;

import com.matejik.terminal.application.concurrent.SharedBackendPool;
import com.matejik.terminal.config.TerminalProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public abstract class CollectionService<T, ID> {

  private final RestClient restClient;
  private final TerminalProperties properties;
  private final SharedBackendPool backendPool;
  private volatile CollectionState<T> cache = CollectionState.empty();

  protected CollectionService(
      RestClient restClient, TerminalProperties properties, SharedBackendPool backendPool) {
    this.restClient = Objects.requireNonNull(restClient, "restClient");
    this.properties = Objects.requireNonNull(properties, "properties");
    this.backendPool = Objects.requireNonNull(backendPool, "backendPool");
  }

  public List<T> fetchItems() {
    return fetchItems(false);
  }

  public CompletableFuture<List<T>> fetchItemsAsync() {
    return fetchItemsAsync(false);
  }

  public CompletableFuture<List<T>> fetchItemsAsync(boolean forceRefresh) {
    return backendPool.supplyAsync(() -> fetchItems(forceRefresh));
  }

  public List<T> fetchItems(boolean forceRefresh) {
    var cached = cache;
    var validCache =
        cached != null
            && !forceRefresh
            && Instant.now().isBefore(cached.lastFetched().plus(properties.api().getCacheTtl()));
    if (validCache) {
      return cached.items();
    }
    var response = restClient.get().uri(resourcePath()).retrieve().body(listType());
    var snapshot =
        new CollectionState<>(List.copyOf(response == null ? List.of() : response), Instant.now());
    cache = snapshot;
    return snapshot.items();
  }

  public T createItem(T item) {
    Objects.requireNonNull(item, "item");
    var created = restClient.post().uri(resourcePath()).body(item).retrieve().body(entityType());
    if (created != null) {
      mutateCache(
          items -> {
            var copy = new java.util.ArrayList<>(items);
            copy.add(created);
            return copy;
          });
      return created;
    }
    return item;
  }

  public CompletableFuture<T> createItemAsync(T item) {
    return backendPool.supplyAsync(() -> createItem(item));
  }

  public T updateItem(T item) {
    Objects.requireNonNull(item, "item");
    var updated =
        restClient.put().uri(itemUri(getId(item))).body(item).retrieve().body(entityType());
    if (updated != null) {
      mutateCache(
          items -> {
            var copy = new java.util.ArrayList<>(items);
            for (int i = 0; i < copy.size(); i++) {
              if (Objects.equals(getId(copy.get(i)), getId(updated))) {
                copy.set(i, updated);
                return copy;
              }
            }
            copy.add(updated);
            return copy;
          });
      return updated;
    }
    return item;
  }

  public CompletableFuture<T> updateItemAsync(T item) {
    return backendPool.supplyAsync(() -> updateItem(item));
  }

  public void deleteItem(T item) {
    Objects.requireNonNull(item, "item");
    restClient.delete().uri(itemUri(getId(item))).retrieve().toBodilessEntity();
    mutateCache(
        items -> {
          var copy = new java.util.ArrayList<>(items);
          copy.removeIf(existing -> Objects.equals(getId(existing), getId(item)));
          return copy;
        });
  }

  public CompletableFuture<Void> deleteItemAsync(T item) {
    return backendPool.runAsync(() -> deleteItem(item));
  }

  protected String itemUri(ID id) {
    return resourcePath() + "/" + URLEncoder.encode(String.valueOf(id), StandardCharsets.UTF_8);
  }

  protected abstract String resourcePath();

  protected abstract Class<T> entityType();

  protected abstract ParameterizedTypeReference<List<T>> listType();

  protected abstract ID getId(T entity);

  private void mutateCache(Function<List<T>, List<T>> mutator) {
    synchronized (this) {
      var current = cache == null ? CollectionState.<T>empty() : cache;
      var mutated = mutator.apply(current.items());
      cache = new CollectionState<>(List.copyOf(mutated), Instant.now());
    }
  }
}
