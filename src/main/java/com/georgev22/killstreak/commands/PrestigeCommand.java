package com.georgev22.killstreak.commands;

import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.killstreak.inventories.PrestigeInventory;
import com.georgev22.killstreak.utilities.MessagesUtil;
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
        if (!(sender instanceof Player)) {
            MinecraftUtils.msg(sender, MessagesUtil.ONLY_PLAYER_COMMAND.getMessagesToString());
            return true;
        }

        Player player = (Player) sender;

        try {
            new PrestigeInventory().openInventory(((Player) sender));
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return true;
    }
}