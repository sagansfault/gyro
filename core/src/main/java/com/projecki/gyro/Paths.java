package com.projecki.gyro;

import com.projecki.gyro.service.ServiceInfo;

public enum Paths {

    GET_SERVERDATA("/serverdata"),
    GET_HUB_REQUEST("/hub/request"), // query params uuid:UUID,
    GET_CONNECT_SERVERGROUP("/servergroup/connect"), // query params uuid:UUID, servergroup:String, priority:QueuePriority
    PUT_LEAVE_QUEUE("/queue/leave"), // query params uuid:UUID
    PUT_WHITELIST_STATUS("/whitelist/status"), // query params servergroup:String, status:Http.Whitelist.Status
    PUT_WHITELIST_USER_STATUS("/whitelist/user/status"), // query params uuid:UUID, servergroup:String, operation:Http.Whitelist.Operation
    ;

    private final String path;

    Paths(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getFullRoute(ServiceInfo info) {
        return "http://" + info.host() + ":" + info.port() + this.path;
    }
}
