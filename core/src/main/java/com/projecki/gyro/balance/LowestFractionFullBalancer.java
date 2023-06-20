package com.projecki.gyro.balance;

import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;

import java.util.Optional;
import java.util.UUID;

public class LowestFractionFullBalancer extends BalanceAlgorithm {

    public LowestFractionFullBalancer() {
        super("lowest-fraction-full-balancer");
    }

    @Override
    public Optional<GyroServer> getTargetServer(UUID user, ServerGroup serverGroup) {
        GyroServer lowest = null;
        for (GyroServer server : serverGroup.getServers()) {
            if (server.getFractionFull() == 1.0) {
                continue;
            }
            if (lowest == null || server.getFractionFull() < lowest.getFractionFull()) {
                lowest = server;
            }
        }
        return Optional.ofNullable(lowest);
    }

    @Override
    public String getId() {
        return "lowest-fraction-full-balancer";
    }
}
