package com.projecki.gyro;

import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.fusion.config.serialize.JacksonSerializer;
import com.projecki.fusion.message.MessageClient;
import com.projecki.gyro.service.ServerGroup;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Map;

public record ServiceConfig(
        String defaultBalancer,
        Map<String, String> customBalancers // map of server group to balancer id
) {

    public static HermesConfig<ServiceConfig> createLoader(String organization,
                                                           RedisAsyncCommands<String, String> commands,
                                                           MessageClient messageClient) {
        return new HermesConfig<>(
                JacksonSerializer.ofYaml(ServiceConfig.class),
                organization,
                "GyroService",
                "config",
                messageClient,
                commands
        );
    }

    public String getBalancer(ServerGroup serverGroup) {
        for (Map.Entry<String, String> entry : customBalancers.entrySet()) {
            String serverGroupId = entry.getKey();
            String balancerId = entry.getValue();
            if (serverGroup.getId().equalsIgnoreCase(serverGroupId)) {
                return balancerId;
            }
        }
        return defaultBalancer;
    }
}
