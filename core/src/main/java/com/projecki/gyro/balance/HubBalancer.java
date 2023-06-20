package com.projecki.gyro.balance;

import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;

import java.util.Optional;
import java.util.UUID;

public class HubBalancer extends BalanceAlgorithm {

    public HubBalancer() {
        super("hub-balancer");
    }

    @Override
    public Optional<GyroServer> getTargetServer(UUID user, ServerGroup serverGroup) {
        GyroServer fullestNonFull = null;
        for (GyroServer server : serverGroup.getServers()) {
            if (server.getFractionFull() < 1.0) {
                if (fullestNonFull == null || fullestNonFull.getPlayers() < server.getPlayers()) {
                    fullestNonFull = server;
                }
            }
        }
        return Optional.ofNullable(fullestNonFull);
    }

    @Override
    public String getId() {
        return "hub-balancer";
    }
}
