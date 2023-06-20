package com.projecki.gyro.listener;

import com.projecki.gyro.GyroVelocity;
import com.projecki.gyro.pojo.Http;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InitialConnectListener {

    private final GyroVelocity gyroVelocity;

    public InitialConnectListener(GyroVelocity gyroVelocity) {
        this.gyroVelocity = gyroVelocity;
    }

    @Subscribe(order = PostOrder.EARLY)
    public EventTask onInitialConnect(PlayerChooseInitialServerEvent event) {
        return EventTask.withContinuation(continuation -> {
            gyroVelocity.getGyroAPI().sendPlayerHub(event.getPlayer().getUniqueId())
                    .orTimeout(2500, TimeUnit.MILLISECONDS)
                    .whenComplete((res, ex) -> {
                        if (res == null && ex == null) {
                            ex = new NullPointerException("Response from service was null");
                        }
                        if (ex != null) {
                            ex.printStackTrace();
                            res = new Http.Hub.Response(null);
                        }
                        Optional.ofNullable(res.hub())
                                .flatMap(s -> gyroVelocity.getProxy().getServer(s))
                                .ifPresentOrElse(event::setInitialServer, () -> {
                                    event.getPlayer().disconnect(Component.text("Could not find suitable hub. Try reconnecting."));
                                });
                        continuation.resume();
                    });
        });
    }
}
