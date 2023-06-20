package com.projecki.gyro.processor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.projecki.fusion.server.BasicServerData;
import com.projecki.gyro.GyroService;
import com.projecki.gyro.manager.ServerGroupManager;
import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ServerDataIntakeProcessor extends Processor {

    private final GyroService service;
    private final ServerGroupManager serverGroupManager;

    public ServerDataIntakeProcessor(GyroService service) {
        super("Server Data Intake Processor", 1000, 250);
        this.service = service;
        this.serverGroupManager = service.getServerGroupManager();
    }

    @Override
    public void runnable() {
        service.getRedis().getServerDataStorage().getAllServerData(BasicServerData.class).thenAccept(set -> {

            Multimap<ServerGroup, GyroServer> servers = HashMultimap.create();
            List<GyroServer> hubs = new ArrayList<>();

            for (BasicServerData serverData : set) {

                Optional<Long> heartbeatOptional = serverData.getHeartbeat();
                Optional<Integer> playerCountOptional = serverData.getPlayerCount();
                Optional<Integer> maxPlayersOptional = serverData.getMaxPlayers();
                Optional<String> serverGroupOptional = serverData.getServerGroup();

                // dont include dead servers
                if (heartbeatOptional.isEmpty() || heartbeatOptional.get() + 600 < System.currentTimeMillis()) {
                    continue;
                }

                // dont include invalid player count no-data servers
                if (playerCountOptional.isPresent() && maxPlayersOptional.isPresent() && serverGroupOptional.isPresent()) {
                    int playerCount = playerCountOptional.get();
                    int maxPlayers = maxPlayersOptional.get();
                    String serverGroup = serverGroupOptional.get();
                    String serverName = serverData.getServerName();

                    if (serverGroup.equalsIgnoreCase("hub")) {
                        hubs.add(new GyroServer(serverName, playerCount, maxPlayers));
                        continue;
                    }

                    Optional<ServerGroup> serverGroupOpt = serverGroupManager.getServerGroup(serverGroup);
                    ServerGroup group;
                    if (serverGroupOpt.isPresent()) {
                        group = serverGroupOpt.get();
                    } else {
                        group = new ServerGroup(serverGroup, new HashSet<>());
                        serverGroupManager.registerIfNotPresent(group);
                    }

                    GyroServer newServer = new GyroServer(serverName, playerCount, maxPlayers);
                    servers.put(group, newServer);
                    group.update(newServer);
                }
            }
            servers.asMap().forEach(ServerGroup::removeDifference);

            ServerGroup hubGroup = serverGroupManager.getHubGroup();
            hubs.forEach(hubGroup::update);
            hubGroup.removeDifference(hubs);
        });
    }
}
