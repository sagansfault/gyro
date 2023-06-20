package com.projecki.gyro.pojo;

import com.projecki.fusion.message.MessageClient;

import java.util.UUID;

public class Redis {

    public static class Velocity {
        public record SendPlayer(UUID uuid, String server, boolean kickIfUnsuccessful) implements MessageClient.Message {
            public SendPlayer(UUID uuid, String server) {
                this(uuid, server, true);
            }
        }
    }
}
