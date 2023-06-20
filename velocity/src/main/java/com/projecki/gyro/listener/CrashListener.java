package com.projecki.gyro.listener;

import com.projecki.gyro.GyroVelocity;
import com.projecki.gyro.pojo.Http;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CrashListener {

    private final GyroVelocity gyroVelocity;

    public CrashListener(GyroVelocity gyroVelocity) {
        this.gyroVelocity = gyroVelocity;
    }

    @Subscribe
    public EventTask onCrash(KickedFromServerEvent event) {
        return EventTask.withContinuation(continuation -> {

            if (event.kickedDuringServerConnect()) {
                continuation.resume();
                return;
            }

            Optional<Component> possibleReason = event.getServerKickReason();
            if (possibleReason.isPresent()) {
                String contentLower = PlainTextComponentSerializer.plainText().serialize(possibleReason.get()).toLowerCase();

                if (contentLower.contains("ban") || !contentLower.contains("crash") && !contentLower.contains("server closed")) {
                    continuation.resume();
                    return;
                }
            }

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
                                .ifPresentOrElse(s -> {
                                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(s));
                                }, () -> {
                                    event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Could not find suitable hub. Try reconnecting.")));
                                });
                        continuation.resume();
                    });
        });
    }
}
