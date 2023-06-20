package com.projecki.gyro.service;

import com.projecki.fusion.message.MessageClient;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public record ServiceInfo(String host, String port) implements MessageClient.Message {

    private static final String HOST_KEY = "gyro:host";
    private static final String PORT_KEY = "gyro:port";

    public static CompletableFuture<Optional<ServiceInfo>> get(RedisAsyncCommands<String, String> redis) {

        CompletableFuture<String> hostFuture = redis.get(HOST_KEY).toCompletableFuture();
        CompletableFuture<String> portFuture = redis.get(PORT_KEY).toCompletableFuture();

        CompletableFuture<Void> all = CompletableFuture.allOf(hostFuture, portFuture);
        return all.thenApply(nothing -> {
            String host;
            String port;
            try {
                host = hostFuture.get();
                port = portFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                return Optional.empty();
            }
            return Optional.of(new ServiceInfo(host, port));
        });
    }

    public CompletableFuture<Void> store(RedisAsyncCommands<String, String> redis) {
        CompletableFuture<String> setHost = redis.set(HOST_KEY, this.host()).toCompletableFuture();
        CompletableFuture<String> setPort = redis.set(PORT_KEY, this.port()).toCompletableFuture();
        return setHost.thenCompose(v -> setPort.thenApply(ignored -> null));
    }
}
