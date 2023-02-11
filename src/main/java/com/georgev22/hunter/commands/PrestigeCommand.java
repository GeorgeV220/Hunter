package com.georgev22.hunter.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.georgev22.hunter.inventories.PrestigeInventory;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

@CommandAlias("prestige")
public class PrestigeCommand extends BaseCommand {

    @SneakyThrows
    @Default
    @Description("{@@commands.descriptions.prestige}")
    @CommandPermission("hunter.prestige")
    public void execute(@NotNull CommandIssuer commandIssuer) {
        if (!commandIssuer.isPlayer()) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), MessagesUtil.ONLY_PLAYER_COMMAND.getMessagesToString());
            return;
        }

        new PrestigeInventory().openInventory(commandIssuer.getIssuer());
    }
}