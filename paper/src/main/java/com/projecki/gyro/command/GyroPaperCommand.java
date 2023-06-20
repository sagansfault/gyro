package com.projecki.gyro.command;

import com.projecki.fusion.command.base.PaperCommonBaseCommand;
import com.projecki.gyro.GyroPaper;
import net.kyori.adventure.text.format.TextColor;

public class GyroPaperCommand extends PaperCommonBaseCommand {

    protected final GyroPaper gyroPaper;

    public GyroPaperCommand(GyroPaper gyroPaper) {
        super(TextColor.fromHexString("#ffd480"), TextColor.fromHexString("#ffbb33"), gyroPaper.getCommandManager());
        this.gyroPaper = gyroPaper;
    }
}
