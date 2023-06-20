package com.projecki.gyro.manager;

import com.projecki.gyro.queue.ServerQueue;
import com.projecki.gyro.service.ServerGroup;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ServerQueueManager {

    private final Set<ServerQueue> queues = new HashSet<>();

    public ServerQueue getQueue(ServerGroup group) {
        for (ServerQueue queue : this.queues) {
            if (queue.getServerGroup().equals(group)) {
                return queue;
            }
        }
        ServerQueue queue = new ServerQueue(group);
        queues.add(queue);
        return queue;
    }

    public Optional<ServerQueue> getQueue(UUID user) {
        for (ServerQueue queue : queues) {
            if (queue.getQueue().contains(user)) {
                return Optional.of(queue);
            }
        }
        return Optional.empty();
    }
}
