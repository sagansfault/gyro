package com.projecki.gyro.service;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GyroServer {

    private static final Timer TIMER = new Timer();

    private final String name;
    private int players;
    private int maxPlayers;
    private int inTransit = 0;

    public GyroServer(String name, int players, int maxPlayers) {
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public GyroServer(String name) {
        this(name, 0, 0);
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players + inTransit;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void putInTransit(int count) {
        if (count < 1) {
            return;
        }
        this.inTransit += count;
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                inTransit -= Math.min(count, inTransit);
            }
        }, 1000);
    }

    public double getFractionFull() {
        return ((double) this.getPlayers()) / ((double) this.maxPlayers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GyroServer that = (GyroServer) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
