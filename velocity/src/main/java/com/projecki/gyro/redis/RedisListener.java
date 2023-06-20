package com.projecki.gyro.redis;

import com.projecki.fusion.message.MessageClient;
import com.projecki.gyro.pojo.Redis;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RedisListener {

    private final ProxyServer proxy;

    public RedisListener(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @MessageClient.MessageListener
    public void connect(String channel, Redis.Velocity.SendPlayer sendPlayer) {

        Optional<Player> playerOpt = proxy.getPlayer(sendPlayer.uuid());
        Optional<RegisteredServer> serverOpt = proxy.getServer(sendPlayer.server());

        CompletableFuture<ConnectionRequestBuilder.Result> connectFuture;

        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();

            if (serverOpt.isEmpty()) {
                connectFuture = CompletableFuture.completedFuture(null);
            } else {
                RegisteredServer server = serverOpt.get();
                connectFuture = player.createConnectionRequest(server).connect();
            }

            connectFuture.thenAccept(result -> {
                if (result == null) {
                    if (sendPlayer.kickIfUnsuccessful()) {
                        player.disconnect(Component.text("Server does not exist, try reconnecting."));
                    }
                    return;
                }
                switch (result.getStatus()) {
                    case SUCCESS, CONNECTION_IN_PROGRESS, ALREADY_CONNECTED -> {}
                    case CONNECTION_CANCELLED, SERVER_DISCONNECTED -> {
                        if (sendPlayer.kickIfUnsuccessful()) {
                            Component message = result.getReasonComponent()
                                    .orElse(Component.text("Unable to connect you to server, try reconnecting."));
                            player.disconnect(message);
                        }
                    }
                }
            });
        }
    }
}
