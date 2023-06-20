package com.projecki.gyro.queue;

import java.util.function.Function;

public enum QueuePriority {
    FIRST("gyro.priority.first"),
    SECOND("gyro.priority.second"),
    THIRD("gyro.priority.third"),
    FOURTH("gyro.priority.fourth"),
    FIFTH("gyro.priority.fifth"),
    SIXTH("gyro.priority.sixth"),
    SEVENTH("gyro.priority.seventh"),
    EIGHTH("gyro.priority.eighth"),
    NINTH("gyro.priority.ninth"),
    TENTH("gyro.priority.tenth"),
    ;

    private final String permission;

    QueuePriority(String permission) {
        this.permission = permission;
    }

    public static QueuePriority getPriority(Function<String, Boolean> permissionCheck) {
        for (QueuePriority value : values()) {
            if (permissionCheck.apply(value.permission)) {
                return value;
            }
        }
        return TENTH;
    }
}
