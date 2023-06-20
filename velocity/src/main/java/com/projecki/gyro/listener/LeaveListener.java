package com.projecki.gyro.listener;

import com.projecki.gyro.GyroVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public class LeaveListener {

    private final GyroVelocity gyroVelocity;

    public LeaveListener(GyroVelocity gyroVelocity) {
        this.gyroVelocity = gyroVelocity;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onInitialConnect(DisconnectEvent event) {
        gyroVelocity.getGyroAPI().removeFromQueue(event.getPlayer().getUniqueId());
    }
}
