package com.projecki.gyro.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.gyro.GyroAPI;
import com.projecki.gyro.GyroPaper;
import com.projecki.gyro.pojo.Http;
import com.projecki.gyro.queue.QueuePriority;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("queue|q")
public class QueueCommand extends GyroPaperCommand {

    public QueueCommand(GyroPaper gyroPaper) {
        super(gyroPaper);
    }

    @Default
    @Description("Connect to a server or join the queue if it's full")
    @Syntax("<servergroup>")
    public void queue(Player player, String serverGroup) {
        GyroAPI api = gyroPaper.getGyroAPI();
        if (serverGroup.equalsIgnoreCase("hub")) {
            Component message = ComponentBuilder.builder("Try ", super.primaryColor)
                    .content("/hub", super.secondaryColor)
                    .toComponent();
            player.sendMessage(message);
        } else {
            api.sendPlayerNonHub(player.getUniqueId(), serverGroup, QueuePriority.getPriority(player::hasPermission)).thenAccept(response -> {
                if (response instanceof Http.ServerConnect.Sending sending) {
                    player.sendMessage(Component.text("Sending you to: ", super.primaryColor)
                            .append(Component.text(sending.server(), super.secondaryColor)));
                } else if (response instanceof Http.ServerConnect.InQueue inQueue) {
                    player.sendMessage(Component.text("Placed in queue: ", super.primaryColor)
                            .append(Component.text(inQueue.place(), super.secondaryColor)));
                } else if (response instanceof Http.ServerConnect.WhitelistedInQueue wlInQueue) {
                    player.sendMessage(Component.text("Server is whitelisted. Placed in queue: ", super.primaryColor)
                            .append(Component.text(wlInQueue.place(), super.secondaryColor)));
                } else if (response instanceof Http.ServerConnect.Error error) {
                    Component message = switch (error) {
                        case ALREADY_IN_QUEUE -> Component.text("You are already in queue for that group", super.primaryColor);
                        case INVALID_SERVER_GROUP -> Component.text("Invalid server group: ", super.primaryColor)
                                .append(Component.text(serverGroup, super.secondaryColor));
                        case INTERNAL_ERROR -> Component.text("An internal error occurred, contact staff!", super.primaryColor);
                    };
                    player.sendMessage(message);
                }
            });
        }
    }
}
