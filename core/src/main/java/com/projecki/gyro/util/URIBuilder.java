package com.projecki.gyro.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class URIBuilder {

    private final String route;
    private final Map<String, String> variables;

    public URIBuilder(String route) {
        this.route = route;
        this.variables = new HashMap<>();
    }

    public URIBuilder variable(String key, String value) {
        variables.put(key, value);
        return this;
    }

    public URI build() {
        String base = route;
        if (!variables.isEmpty()) {
            base += "?";
        }

        String variableString = variables.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        base += variableString;

        return URI.create(base);
    }
}
