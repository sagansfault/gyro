package com.projecki.gyro.util;

import com.projecki.fusion.serializer.formatted.JacksonSerializer;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ReqBuilder<T> {

    private static final JacksonSerializer SERIALIZER = JacksonSerializer.ofJson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final URI uri;
    private final Class<T> responseType;
    private Type reqType = Type.GET;
    private @Nullable Object body;

    public ReqBuilder(URI uri, Class<T> responseType) {
        this.uri = uri;
        this.responseType = responseType;
    }

    public ReqBuilder<T> GET() {
        body = null;
        return this;
    }

    public ReqBuilder<T> POST(@Nullable Object body) {
        this.reqType = Type.POST;
        this.body = body;
        return this;
    }

    public ReqBuilder<T> PUT(@Nullable Object body) {
        this.reqType = Type.PUT;
        this.body = body;
        return this;
    }

    public CompletableFuture<T> send() {
        HttpRequest.Builder builder = HttpRequest.newBuilder(this.uri);

        HttpRequest.BodyPublisher bodyPublisher;
        if (body == null) {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(SERIALIZER.serialize(body));
        }

        builder = switch (this.reqType) {
            case GET -> builder.GET();
            case POST -> builder.POST(bodyPublisher);
            case PUT -> builder.PUT(bodyPublisher);
        };

        return HTTP_CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> SERIALIZER.deserialize(responseType, res.body()));
    }

    public enum Type {
        GET, POST, PUT
    }
}
