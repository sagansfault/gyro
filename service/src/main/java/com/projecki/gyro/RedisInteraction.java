package com.projecki.gyro;

import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.message.redis.RedisMessageClient;
import com.projecki.fusion.server.RedisServerDataStorage;
import com.projecki.fusion.server.ServerDataStorage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;

public class RedisInteraction {

    private final RedisAsyncCommands<String, String> commands;
    private final MessageClient messageClient;
    private final ServerDataStorage serverDataStorage;

    public RedisInteraction(GyroService.Creds creds) {
        RedisClient client = RedisClient.create(RedisURI.builder()
                .withHost(creds.redisHost())
                .withPort(Integer.parseInt(creds.redisPort()))
                .withPassword(creds.redisPass()).build()
        );

        commands = client.connect().async();
        messageClient = new RedisMessageClient(commands, client.connectPubSub().async());
        serverDataStorage = new RedisServerDataStorage(client);
    }

    public RedisAsyncCommands<String, String> getCommands() {
        return commands;
    }

    public MessageClient getMessageClient() {
        return messageClient;
    }

    public ServerDataStorage getServerDataStorage() {
        return serverDataStorage;
    }
}
