package com.projecki.gyro.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.projecki.gyro.GyroPaper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("hub")
public class HubCommand extends GyroPaperCommand {

    public HubCommand(GyroPaper gyroPaper) {
        super(gyroPaper);
    }

    @Default
    @Description("Go back to the hub")
    public void hub(Player player) {
        gyroPaper.getGyroAPI().sendPlayerHub(player.getUniqueId()).thenAccept(response -> {
            if (response.hub() == null) {
                player.kick(Component.text("Unable to connect you to a hub, try reconnecting"));
            }
        });
    }
}
