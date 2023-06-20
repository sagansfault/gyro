package com.projecki.gyro.queue;

import com.projecki.fusion.util.SortedScoredSet;
import com.projecki.gyro.service.ServerGroup;

import java.util.Objects;
import java.util.UUID;

public class ServerQueue {

    private final ServerGroup serverGroup;
    private final SortedScoredSet<UUID> queue;

    public ServerQueue(ServerGroup serverGroup) {
        this.serverGroup = serverGroup;
        queue = new SortedScoredSet<>();
    }

    public ServerGroup getServerGroup() {
        return serverGroup;
    }

    public SortedScoredSet<UUID> getQueue() {
        return queue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerQueue that = (ServerQueue) o;
        return serverGroup.equals(that.serverGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverGroup);
    }
}
