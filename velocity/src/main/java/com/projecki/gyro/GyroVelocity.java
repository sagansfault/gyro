package com.projecki.gyro;

import co.aikar.commands.VelocityCommandManager;
import com.google.inject.Inject;
import com.projecki.fusion.FusionVelocity;
import com.projecki.gyro.listener.CrashListener;
import com.projecki.gyro.listener.InitialConnectListener;
import com.projecki.gyro.listener.LeaveListener;
import com.projecki.gyro.redis.RedisListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(id = "gyro-velocity",
        name = "GyroVelocity",
        version = "1.0-SNAPSHOT",
        authors = {"sagan"},
        dependencies = {
            @Dependency(id = "fusion-velocity")
        })
public class GyroVelocity {

    private final ProxyServer proxy;
    private final Logger logger;
    private VelocityCommandManager commandManager;

    private GyroAPI gyroAPI;

    @Inject
    public GyroVelocity(ProxyServer server, Logger logger) {
        this.proxy = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        commandManager = new VelocityCommandManager(proxy, this);
        proxy.getEventManager().register(this, new CrashListener(this));
        proxy.getEventManager().register(this, new InitialConnectListener(this));
        proxy.getEventManager().register(this, new LeaveListener(this));

        proxy.getScheduler().buildTask(this, () -> {
            GyroAPI.create(FusionVelocity.getRedisCommands())
                    .completeOnTimeout(Optional.empty(), 2, TimeUnit.SECONDS)
                    .thenAccept(opt -> {
                        if (opt.isEmpty()) {
                            getLogger().error("Could not load service info for GyroAPI, try restarting");
                            proxy.shutdown();
                        } else {
                            gyroAPI = opt.get();
                        }
                    });
            FusionVelocity.getMessageClient().registerMessageListener(new RedisListener(proxy));
        }).delay(Duration.ofSeconds(3)).schedule();
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public VelocityCommandManager getCommandManager() {
        return commandManager;
    }

    public GyroAPI getGyroAPI() {
        return gyroAPI;
    }
}
