package com.georgev22.hunter.commands;

import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.hunter.inventories.PrestigeInventory;
import com.georgev22.hunter.utilities.MessagesUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class PrestigeCommand extends BukkitCommand {


    public PrestigeCommand() {
        super("prestige");
        this.description = "Prestige command";
        this.usageMessage = "/prestige";
        this.setPermission("killstreak.prestige");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessagesToString()));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player player)) {
            MinecraftUtils.msg(sender, MessagesUtil.ONLY_PLAYER_COMMAND.getMessagesToString());
            return true;
        }

        try {
            new PrestigeInventory().openInventory(player);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }

        return true;
    }
}