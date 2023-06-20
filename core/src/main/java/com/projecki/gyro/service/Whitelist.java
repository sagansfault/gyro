package com.projecki.gyro.service;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class Whitelist implements Iterable<UUID> {
    private boolean active;
    private final Set<UUID> whitelist;

    public Whitelist(boolean active, Set<UUID> whitelist) {
        this.active = active;
        this.whitelist = whitelist;
    }

    public Whitelist() {
        this(false, new HashSet<>());
    }

    public boolean contains(UUID uuid) {
        return whitelist.contains(uuid);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<UUID> whitelist() {
        return whitelist;
    }

    @NotNull
    @Override
    public Iterator<UUID> iterator() {
        return whitelist.iterator();
    }
}
