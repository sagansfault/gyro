package com.projecki.gyro.service;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ServerGroup {

    private final String id;
    private final Set<GyroServer> servers;
    private final Whitelist whitelist;

    public ServerGroup(String id, Set<GyroServer> servers) {
        this.id = id;
        this.servers = new HashSet<>(servers);
        this.whitelist = new Whitelist();
    }

    public String getId() {
        return id;
    }

    @Unmodifiable
    public Set<GyroServer> getServers() {
        return Collections.unmodifiableSet(this.servers);
    }

    public void removeDifference(Collection<GyroServer> positive) {
        this.servers.removeIf(gyroServer -> !positive.contains(gyroServer));
    }

    public void clearAndPutAll(Set<GyroServer> servers) {
        this.servers.clear();
        this.servers.addAll(servers);
    }

    public void update(GyroServer newServer) {
        boolean found = false;
        for (GyroServer server : this.servers) {
            if (server.equals(newServer)) {
                server.setPlayers(newServer.getPlayers());
                server.setMaxPlayers(newServer.getMaxPlayers());
                found = true;
                break;
            }
        }
        if (!found) {
            this.servers.add(newServer);
        }
    }

    public Whitelist getWhitelist() {
        return whitelist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerGroup that = (ServerGroup) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
