package com.projecki.gyro;

import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.fusion.serializer.formatted.JacksonSerializer;
import com.projecki.gyro.balance.BalanceAlgorithm;
import com.projecki.gyro.manager.ServerGroupManager;
import com.projecki.gyro.manager.ServerQueueManager;
import com.projecki.gyro.pojo.Http;
import com.projecki.gyro.pojo.Redis;
import com.projecki.gyro.processor.QueueProcessor;
import com.projecki.gyro.processor.ServerDataIntakeProcessor;
import com.projecki.gyro.queue.QueuePriority;
import com.projecki.gyro.queue.ServerQueue;
import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;
import com.projecki.gyro.service.ServiceInfo;
import io.javalin.Javalin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GyroService {

    private final Javalin javalin;
    private final JacksonSerializer serializer;
    private final GyroAPI gyroAPI;

    private final RedisInteraction redis;
    private final ServerGroupManager serverGroupManager;
    private final ServerQueueManager serverQueueManager;
    private ServiceConfig config;

    public GyroService(Creds creds) {
        this.redis = new RedisInteraction(creds);

        HermesConfig<ServiceConfig> loader = ServiceConfig.createLoader(creds.organization(), redis.getCommands(), redis.getMessageClient());
        Optional<ServiceConfig> serviceConfigOpt;
        try {
            serviceConfigOpt = loader.loadConfig().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        config = serviceConfigOpt.orElseThrow(() -> new IllegalStateException("Config could not be deserialized"));
        loader.onUpdate(opt -> opt.ifPresent(conf -> config = conf));

        this.javalin = Javalin.create().start(creds.host(), Integer.parseInt(creds.port()));
        this.initializeEndPoints();
        this.serializer = JacksonSerializer.ofJson();

        ServiceInfo serviceInfo = new ServiceInfo(creds.host(), creds.port());
        serviceInfo.store(redis.getCommands()).thenAccept(nothing -> {
            System.out.println("Stored and sending ServiceInfo for other programs to use");
            redis.getMessageClient().send(serviceInfo);
        });
        this.gyroAPI = new GyroAPI(serviceInfo);
        serviceInfo.store(redis.getCommands());

        this.serverGroupManager = new ServerGroupManager();
        this.serverQueueManager = new ServerQueueManager();
        QueueProcessor queueProcessor = new QueueProcessor(this);
        ServerDataIntakeProcessor intakeProcessor = new ServerDataIntakeProcessor(this);
        queueProcessor.start();
        intakeProcessor.start();

        System.out.println("Service running");
    }

    public void initializeEndPoints() {
        javalin.get(Paths.GET_SERVERDATA.getPath(), context -> {
            Map<ServerGroup, ServerQueue> serverData = serverGroupManager.getServerGroups().stream()
                    .collect(Collectors.toMap(sg -> sg, serverQueueManager::getQueue));
            context.result(serializer.serialize(new Http.ServerData(serverData)));
        });

        javalin.get(Paths.GET_HUB_REQUEST.getPath(), context -> {
            String uuidString = context.queryParam("uuid");
            if (uuidString == null) {
                return;
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }
            Optional<GyroServer> targetServer = BalanceAlgorithm.Instance.HUB.getInstance().getTargetServer(uuid, serverGroupManager.getHubGroup());
            Http.Hub.Response response;
            if (targetServer.isPresent()) {
                GyroServer server = targetServer.get();
                server.putInTransit(1);
                redis.getMessageClient().send(new Redis.Velocity.SendPlayer(uuid, server.getName()));
                response = new Http.Hub.Response(server.getName());
                System.out.println("(" + uuid + ")" + " Got Hub Request - Sending to: " + server.getName());
            } else {
                response = new Http.Hub.Response(null);
                System.out.println("(" + uuid + ")" + " Got Hub Request - No server to send to");
            }
            context.result(serializer.serialize(response));
        });

        javalin.get(Paths.GET_CONNECT_SERVERGROUP.getPath(), context -> {
            String uuidString = context.queryParam("uuid");
            String serverGroupString = context.queryParam("servergroup");
            String queuePriorityString = context.queryParam("priority");
            if (uuidString == null || serverGroupString == null || queuePriorityString == null) {
                return;
            }
            UUID uuid;
            QueuePriority queuePriority;
            try {
                uuid = UUID.fromString(uuidString);
                queuePriority = QueuePriority.valueOf(queuePriorityString);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }

            Optional<ServerGroup> serverGroupOpt = serverGroupManager.getServerGroup(serverGroupString);
            Http.ServerConnect.Response response;
            if (serverGroupOpt.isPresent()) {
                ServerGroup serverGroup = serverGroupOpt.get();

                Optional<ServerQueue> queueOpt = serverQueueManager.getQueue(uuid);
                if (queueOpt.isPresent()) {
                    ServerQueue queue = queueOpt.get();
                    if (queue.getServerGroup().equals(serverGroup)) {
                        response = Http.ServerConnect.Error.ALREADY_IN_QUEUE;
                        context.result(serializer.serialize(response));
                        return;
                    } else {
                        queue.getQueue().remove(uuid);
                    }
                }

                if (serverGroup.getWhitelist().isActive() && !serverGroup.getWhitelist().contains(uuid)) {
                    int place = serverQueueManager.getQueue(serverGroup).getQueue().put(uuid, -queuePriority.ordinal()) + 1;
                    response = new Http.ServerConnect.WhitelistedInQueue(place);
                    System.out.println("(" + uuid + ")" + " Got Queue Request - Server group whitelisted, placed in queue");
                } else {
                    String balancerId = config.getBalancer(serverGroup);
                    BalanceAlgorithm balancer = BalanceAlgorithm.Instance.getInstance(balancerId);
                    Optional<GyroServer> targetServerOpt = balancer.getTargetServer(uuid, serverGroup);

                    if (targetServerOpt.isEmpty()) {
                        // negative queue priority ordinal on purpose
                        int place = serverQueueManager.getQueue(serverGroup).getQueue().put(uuid, -queuePriority.ordinal()) + 1;
                        response = new Http.ServerConnect.InQueue(place);
                        System.out.println("(" + uuid + ")" + " Got Queue Request - No non-full servers present, placed in queue");
                    } else {
                        GyroServer gyroServer = targetServerOpt.get();
                        response = new Http.ServerConnect.Sending(gyroServer.getName());
                        redis.getMessageClient().send(new Redis.Velocity.SendPlayer(uuid, gyroServer.getName()));
                        System.out.println("(" + uuid + ")" + " Got Queue Request - Space open, sending to: " + gyroServer.getName());
                    }
                }
                context.result(serializer.serialize(response));
            } else {
                response = Http.ServerConnect.Error.INVALID_SERVER_GROUP;
                System.out.println("(" + uuid + ")" + " Got Queue Request - Invalid server group");
                context.result(serializer.serialize(response));
            }
        });

        javalin.put(Paths.PUT_LEAVE_QUEUE.getPath(), context -> {
            String uuidString = context.queryParam("uuid");
            if (uuidString == null) {
                return;
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }

            Http.LeaveQueue.Response response;

            Optional<ServerQueue> queueOpt = serverQueueManager.getQueue(uuid);
            if (queueOpt.isPresent()) {
                ServerQueue queue = queueOpt.get();
                queue.getQueue().remove(uuid);
                response = new Http.LeaveQueue.Response(null);
            } else {
                response = new Http.LeaveQueue.Response(Http.LeaveQueue.Error.NOT_IN_QUEUE);
            }
            context.result(serializer.serialize(response));
        });

        javalin.put(Paths.PUT_WHITELIST_STATUS.getPath(), context -> {
            String serverGroupString = context.queryParam("servergroup");
            String whitelistStatusString = context.queryParam("status");
            if (serverGroupString == null || whitelistStatusString == null) {
                return;
            }
            boolean status = Boolean.parseBoolean(whitelistStatusString);

            Optional<ServerGroup> serverGroupOpt = serverGroupManager.getServerGroup(serverGroupString);
            if (serverGroupOpt.isPresent()) {
                ServerGroup serverGroup = serverGroupOpt.get();
                serverGroup.getWhitelist().setActive(status);
                Http.Whitelist.Response response = new Http.Whitelist.Response(null);
                context.result(serializer.serialize(response));
            } else {
                Http.Whitelist.Response response = new Http.Whitelist.Response(Http.Whitelist.Error.INVALID_SERVERGROUP);
                context.result(serializer.serialize(response));
            }
        });

        javalin.put(Paths.PUT_WHITELIST_USER_STATUS.getPath(), context -> {
            String uuidString = context.queryParam("uuid");
            String serverGroupString = context.queryParam("servergroup");
            String operationString = context.queryParam("operation");
            if (uuidString == null || serverGroupString == null || operationString == null) {
                return;
            }
            UUID uuid;
            Http.Whitelist.Operation operation;
            try {
                uuid = UUID.fromString(uuidString);
                operation = Http.Whitelist.Operation.valueOf(operationString);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }

            Optional<ServerGroup> serverGroupOpt = serverGroupManager.getServerGroup(serverGroupString);
            if (serverGroupOpt.isPresent()) {
                ServerGroup serverGroup = serverGroupOpt.get();
                switch (operation) {
                    case ADD -> serverGroup.getWhitelist().whitelist().add(uuid);
                    case REMOVE -> serverGroup.getWhitelist().whitelist().remove(uuid);
                }
                Http.Whitelist.Response response = new Http.Whitelist.Response(null);
                context.result(serializer.serialize(response));
            } else {
                Http.Whitelist.Response response = new Http.Whitelist.Response(Http.Whitelist.Error.INVALID_SERVERGROUP);
                context.result(serializer.serialize(response));
            }
        });
    }

    public JacksonSerializer getSerializer() {
        return serializer;
    }

    public RedisInteraction getRedis() {
        return redis;
    }

    public GyroAPI getGyroAPI() {
        return gyroAPI;
    }

    public ServerGroupManager getServerGroupManager() {
        return serverGroupManager;
    }

    public ServerQueueManager getServerQueueManager() {
        return serverQueueManager;
    }

    public ServiceConfig getConfig() {
        return config;
    }

    public record Creds(String redisHost, String redisPort, String redisPass, String host, String port, String organization) {}
}
