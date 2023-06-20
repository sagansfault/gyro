package com.projecki.gyro.manager;

import com.projecki.gyro.service.ServerGroup;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ServerGroupManager {

    private final Set<ServerGroup> serverGroups;
    private final ServerGroup hubGroup;

    public ServerGroupManager() {
        this.serverGroups = new HashSet<>();
        this.hubGroup = new ServerGroup("hub", new HashSet<>());
    }

    @Unmodifiable
    public Set<ServerGroup> getServerGroups() {
        return Collections.unmodifiableSet(serverGroups);
    }

    public ServerGroup getHubGroup() {
        return hubGroup;
    }

    public Optional<ServerGroup> getServerGroup(String id) {
        for (ServerGroup serverGroup : serverGroups) {
            if (serverGroup.getId().equalsIgnoreCase(id)) {
                return Optional.of(serverGroup);
            }
        }
        return Optional.empty();
    }

    public void registerIfNotPresent(ServerGroup serverGroup) {
        this.serverGroups.add(serverGroup);
    }

    public ServerGroup getOrCreate(String id, ServerGroup replacement) {
        ServerGroup returnable = replacement;
        for (ServerGroup serverGroup : this.serverGroups) {
            if (serverGroup.getId().equalsIgnoreCase(id)) {
                returnable = serverGroup;
                break;
            }
        }
        return returnable;
    }
}
