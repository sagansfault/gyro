package com.projecki.gyro.processor;

import com.projecki.gyro.GyroService;
import com.projecki.gyro.balance.BalanceAlgorithm;
import com.projecki.gyro.pojo.Redis;
import com.projecki.gyro.queue.ServerQueue;
import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;

import java.util.Optional;
import java.util.UUID;

public class QueueProcessor extends Processor {

    private final GyroService service;

    public QueueProcessor(GyroService service) {
        super("Queue Processor", 1000, 1000);
        this.service = service;
    }

    @Override
    public void runnable() {
        outer:
        for (ServerGroup serverGroup : service.getServerGroupManager().getServerGroups()) {
            ServerQueue queue = service.getServerQueueManager().getQueue(serverGroup);
            if (queue.getQueue().isEmpty()) {
                continue;
            }

            int available = serverGroup.getServers().stream()
                    .mapToInt(server -> server.getMaxPlayers() - server.getPlayers())
                    .sum();
            if (available == 0) { // also 0 for empty stream, (ie.) no servers in the server group
                continue;
            }

            String balancerId = service.getConfig().getBalancer(serverGroup);
            BalanceAlgorithm balancer = BalanceAlgorithm.Instance.getInstance(balancerId);

            // process 5 players at a time (per period=1sec at time of writing)
            for (int i = 0; i < 5; i++) {
                if (serverGroup.equals(service.getServerGroupManager().getHubGroup())) {
                    Optional<UUID> uuidOpt = queue.getQueue().get(0);
                    if (uuidOpt.isPresent()) {
                        UUID uuid = uuidOpt.get();
                        Optional<GyroServer> targetServerOpt = balancer.getTargetServer(uuid, serverGroup);

                        // very odd case in which the balancer deems that there is no fit server
                        if (targetServerOpt.isEmpty()) {
                            continue outer;
                        }

                        GyroServer server = targetServerOpt.get();
                        server.putInTransit(1);
                        service.getRedis().getMessageClient().send(new Redis.Velocity.SendPlayer(uuid, server.getName()));
                    }
                }
            }
        }
    }
}
