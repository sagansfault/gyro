package com.projecki.gyro.pojo;

import com.projecki.gyro.queue.ServerQueue;
import com.projecki.gyro.service.ServerGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class Http {

    public static class Hub {

        /**
         * The response given by the service when a request of a valid hub is made.
         *
         * @param hub The hub which the service is responding with, or an empty optional if one could not be found (rare
         *            edge-case)
         */
        public record Response(@Nullable String hub) {}
    }

    public static class ServerConnect {

        /**
         * A super type of all permitted responses this server-connect could return
         */
        public sealed interface Response permits InQueue, WhitelistedInQueue, Sending, Error {}

        /**
         * A response indicating that the user was placed in the queue for a server group.
         *
         * @param place Their place in queue
         */
        public record InQueue(int place) implements Response {}

        /**
         * A response indicating that the requested server group has an active whitelist and the user is not on it. Thus
         * they have been placed in a queue for that server group.
         *
         * @param place Their place in queue
         */
        public record WhitelistedInQueue(int place) implements Response {}

        /**
         * A response indicating that the user has been sent to a server in the requested server group
         *
         * @param server The server they were sent to
         */
        public record Sending(String server) implements Response {}

        public enum Error implements Response {
            /**
             * An error indicating that the user was already in the queue for the requested sever group
             */
            ALREADY_IN_QUEUE,
            /**
             * An error indicating that the requested server group is invalid.
             */
            INVALID_SERVER_GROUP,
            /**
             * An error indicating that the service experienced an internal error while processing the request
             */
            INTERNAL_ERROR
        }
    }

    public static class LeaveQueue {

        /**
         * Represents a response given by the service when a request is made for a player to leave a queue they might
         * be present in.
         *
         * @param possibleError A possible error given by the service when trying to perform the requested operation
         */
        public record Response(@Nullable Error possibleError) {}

        /**
         * Possible errors requests of this type might produce.
         */
        public enum Error {
            /**
             * An error given if the user is not in any queue to begin with. This has no destructive side effects and
             * can be thought of a notice (FYI) more than an error.
             */
            NOT_IN_QUEUE,
            /**
             * Represents all internal errors not pertaining to the API's desired functionality. Examples include the
             * service info not being present for the API functions to use to send requests to the service.
             */
            INTERNAL_ERROR
        }
    }

    public static class Whitelist {

        public record Response(@Nullable Error possibleError) {}

        public record ManageStatus(String serverGroup, boolean active) {}

        public record ManageUser(String serverGroup, UUID user, Operation operation) {}

        public enum Operation {
            ADD, REMOVE;
        }

        public enum Error {
            INVALID_SERVERGROUP, INTERNAL_ERROR;
        }
    }

    public record ServerData(Map<ServerGroup, ServerQueue> serverData) {}
}
