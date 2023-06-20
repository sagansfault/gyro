package com.projecki.gyro.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.gyro.GyroPaper;
import com.projecki.gyro.pojo.Http;
import com.projecki.gyro.queue.ServerQueue;
import com.projecki.gyro.service.GyroServer;
import com.projecki.gyro.service.ServerGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.Map;

@CommandAlias("gyro")
@CommandPermission("gyro.admin")
public class GyroCommand extends GyroPaperCommand {

    public GyroCommand(GyroPaper gyroPaper) {
        super(gyroPaper);
    }

    @Subcommand("info")
    public void info(CommandSender sender) {
        gyroPaper.getGyroAPI().getServerData().thenAccept(serverData -> {
            ComponentBuilder builder = ComponentBuilder.builder().newLine();

            for (Map.Entry<ServerGroup, ServerQueue> entry : serverData.serverData().entrySet()) {
                ServerGroup serverGroup = entry.getKey();
                ServerQueue serverQueue = entry.getValue();
                builder.content(serverGroup.getId(), primaryColor)
                        .content(" (wl:").content(String.valueOf(serverGroup.getWhitelist().isActive()))
                        .content(", q:").content(String.valueOf(serverQueue.getQueue().size()))
                        .content(")").content(" > ", primaryColor);
                if (serverGroup.getServers().isEmpty()) {
                    builder.content("None");
                } else {
                    Iterator<GyroServer> iterator = serverGroup.getServers().iterator();
                    while (iterator.hasNext()) {
                        GyroServer server = iterator.next();
                        double percentFull = (double) server.getPlayers() / (double) server.getMaxPlayers();
                        NamedTextColor color = percentFull >= 0.9 ? NamedTextColor.RED : percentFull >= 0.60 ? NamedTextColor.YELLOW : NamedTextColor.GREEN;
                        builder.content(server.getName() + " (" + server.getPlayers() + "/" + server.getMaxPlayers() + ")", color);
                        if (iterator.hasNext()) {
                            builder.content(", ", primaryColor);
                        }
                    }
                }
                builder.newLine();
            }

            sender.sendMessage(builder.toComponent());
        });
    }

    @Subcommand("whitelistset")
    @Syntax("<servergroup> <active:true|false>")
    @CommandCompletion("@nothing @statuses")
    @Description("Changes whether the status of the whitelist on a servergroup")
    public void whitelistSet(CommandSender sender, String serverGroup, boolean active) {
        gyroPaper.getGyroAPI().setWhitelistStatus(serverGroup, active).thenAccept(res -> handleResponse(sender, res));
    }

    @Subcommand("whitelistmanage")
    @Syntax("<servergroup> <operation> <user>")
    @CommandCompletion("@nothing @operations @nothing")
    @Description("Changes the status of a user in the whitelist of a servergroup")
    public void whitelistManage(CommandSender sender, String serverGroup, Http.Whitelist.Operation operation, @Single String user) {
        FusionPaper.getNameResolver().resolveUuidMojang(user).thenAccept(uuidOpt -> {
            if (uuidOpt.isPresent()) {
                gyroPaper.getGyroAPI().setWhitelistUserStatus(serverGroup, uuidOpt.get(), operation).thenAccept(res -> handleResponse(sender, res));
            } else {
                sender.sendMessage(Component.text("Could not find user", super.primaryColor));
            }
        });
    }

    private void handleResponse(CommandSender sender, Http.Whitelist.Response response) {
        if (response.possibleError() != null) {
            Component message = switch (response.possibleError()) {
                case INVALID_SERVERGROUP -> Component.text("Invalid servergroup", super.primaryColor);
                case INTERNAL_ERROR -> Component.text("Internal service error", super.primaryColor);
            };
            sender.sendMessage(message);
        }
    }
}
