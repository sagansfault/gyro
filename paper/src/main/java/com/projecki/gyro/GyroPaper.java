package com.projecki.gyro;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.projecki.fusion.FusionPaper;
import com.projecki.gyro.command.GyroCommand;
import com.projecki.gyro.command.HubCommand;
import com.projecki.gyro.command.QueueCommand;
import com.projecki.gyro.pojo.Http;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GyroPaper extends JavaPlugin {

    private PaperCommandManager commandManager;
    private GyroAPI gyroAPI;

    @Override
    public void onEnable() {
        commandManager = new PaperCommandManager(this);
        registerCommands();

        GyroAPI.create(FusionPaper.getRedisCommands())
                .completeOnTimeout(Optional.empty(), 2, TimeUnit.SECONDS)
                .thenAccept(opt -> {
                    if (opt.isEmpty()) {
                        getLogger().severe("Could not load service info for GyroAPI, try restarting");
                        getServer().getPluginManager().disablePlugin(this);
                    } else {
                        gyroAPI = opt.get();
                    }
                });
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public GyroAPI getGyroAPI() {
        return gyroAPI;
    }

    public void registerCommands() {
        commandManager.registerCommand(new HubCommand(this));
        commandManager.registerCommand(new QueueCommand(this));
        commandManager.registerCommand(new GyroCommand(this));

        commandManager.getCommandContexts().registerContext(Http.Whitelist.Operation.class, c -> {
            String first = c.popFirstArg();
            try {
                return Http.Whitelist.Operation.valueOf(first);
            } catch (IllegalArgumentException ex) {
                throw new InvalidCommandArgument("Invalid operation", true);
            }
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("operations", handler -> {
            return Arrays.stream(Http.Whitelist.Operation.values()).map(Enum::toString).toList();
        });
    }
}