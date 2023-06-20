package com.projecki.gyro;

import com.projecki.gyro.pojo.Http;
import com.projecki.gyro.queue.QueuePriority;
import com.projecki.gyro.service.ServiceInfo;
import com.projecki.gyro.util.ReqBuilder;
import com.projecki.gyro.util.URIBuilder;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GyroAPI {

    private final ServiceInfo serviceInfo;

    public GyroAPI(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public static CompletableFuture<Optional<GyroAPI>> create(RedisAsyncCommands<String, String> redis) {
        return ServiceInfo.get(redis).thenApply(opt -> {
            if (opt.isPresent()) {
                ServiceInfo serviceInfo = opt.get();
                return Optional.of(new GyroAPI(serviceInfo));
            } else {
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<Http.ServerConnect.Response> sendPlayerNonHub(UUID user, String serverGroup, QueuePriority queuePriority) {
        if (serverGroup.equalsIgnoreCase("hub")) {
            throw new IllegalArgumentException("'hub' invalid non-hub server group. Use 'hub' specific api method.");
        }
        String fullRoute = Paths.GET_CONNECT_SERVERGROUP.getFullRoute(serviceInfo);
        URIBuilder uriBuilder = new URIBuilder(fullRoute)
                .variable("uuid", user.toString())
                .variable("servergroup", serverGroup)
                .variable("priority", queuePriority.name());
        return new ReqBuilder<>(uriBuilder.build(), Http.ServerConnect.Response.class).GET().send();
    }

    public CompletableFuture<Http.Hub.Response> sendPlayerHub(UUID user) {
        String fullRoute = Paths.GET_HUB_REQUEST.getFullRoute(serviceInfo);
        URIBuilder uriBuilder = new URIBuilder(fullRoute)
                .variable("uuid", user.toString());
        return new ReqBuilder<>(uriBuilder.build(), Http.Hub.Response.class).GET().send();
    }

    public CompletableFuture<Http.ServerData> getServerData() {
        String fullRoute = Paths.GET_SERVERDATA.getFullRoute(serviceInfo);
        URIBuilder uriBuilder = new URIBuilder(fullRoute);
        return new ReqBuilder<>(uriBuilder.build(), Http.ServerData.class).GET().send();
    }

    public CompletableFuture<Http.Whitelist.Response> setWhitelistStatus(String serverGroup, boolean active) {
        String fullRoute = Paths.PUT_WHITELIST_STATUS.getFullRoute(serviceInfo);
        URIBuilder uriBuilder = new URIBuilder(fullRoute)
                .variable("servergroup", serverGroup)
                .variable("status", String.valueOf(active));
        return new ReqBuilder<>(uriBuilder.build(), Http.Whitelist.Response.class).PUT(null).send();
    }

    public CompletableFuture<Http.Whitelist.Response> setWhitelistUserStatus(String serverGroup, UUID user, Http.Whitelist.Operation operation) {
        String fullRoute = Paths.PUT_WHITELIST_USER_STATUS.getFullRoute(serviceInfo);
        URIBuilder uriBuilder = new URIBuilder(fullRoute)
                .variable("servergroup", serverGroup)
                .variable("uuid", user.toString())
                .variable("operation", operation.toString());
        return new ReqBuilder<>(uriBuilder.build(), Http.Whitelist.Response.class).PUT(null).send();
    }

    public CompletableFuture<Http.LeaveQueue.Response> removeFromQueue(UUID user) {
        URIBuilder uriBuilder = new URIBuilder(Paths.PUT_LEAVE_QUEUE.getFullRoute(serviceInfo))
                .variable("uuid", user.toString());
        return new ReqBuilder<>(uriBuilder.build(), Http.LeaveQueue.Response.class)
                .PUT(null)
                .send();
    }
}