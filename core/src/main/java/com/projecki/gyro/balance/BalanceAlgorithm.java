package com.projecki.gyro.balance;

import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;

import java.util.Optional;
import java.util.UUID;

public abstract class BalanceAlgorithm {

    private final String id;

    public BalanceAlgorithm(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract Optional<GyroServer> getTargetServer(UUID user, ServerGroup serverGroup);

    public enum Instance {
        HUB(new HubBalancer()),
        LOWEST_FRACTION_FULL(new LowestFractionFullBalancer())
        ;

        private final BalanceAlgorithm balancer;

        Instance(BalanceAlgorithm balancer) {
            this.balancer = balancer;
        }

        public BalanceAlgorithm getInstance() {
            return balancer;
        }

        public static BalanceAlgorithm getInstance(String id) {
            for (Instance value : values()) {
                if (value.getInstance().getId().equalsIgnoreCase(id)) {
                    return value.getInstance();
                }
            }
            return LOWEST_FRACTION_FULL.getInstance();
        }
    }
}
