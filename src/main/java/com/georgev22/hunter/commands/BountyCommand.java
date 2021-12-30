package com.georgev22.hunter.commands;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.player.UserData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BountyCommand extends BukkitCommand {


    public BountyCommand() {
        super("bounty");
        this.description = "bounty command";
        this.usageMessage = "/bounty";
        this.setPermission("hunter.bounty");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return true;
            }
            MessagesUtil.BOUNTY_PLAYER.msg(sender, UserData.getUser((Player) sender).user().placeholders(), true);
            return true;
        }
        if (args.length == 1) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(sender);
                return true;
            }
            MessagesUtil.BOUNTY_PLAYER_OTHER.msg(sender, UserData.getUser(offlinePlayer).user().placeholders(), true);
            return true;
        }

        if (args.length > 1) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(sender);
                return true;
            }
            double value = Double.parseDouble(args[1]);
            if (value <= 0) {
                MessagesUtil.TRANSACTION_ERROR_NEGATIVE.msg(sender, ObjectMap.newHashObjectMap().append("%player%", sender.getName()).append("%transaction%", String.valueOf(value)), true);
                return true;
            }
            UserData userData = UserData.getUser(offlinePlayer);
            if (!(sender instanceof Player)) {
                userData.setBounty(userData.getBounty() + value);
                MessagesUtil.BOUNTY_PLAYER_SET.msgAll(userData.user().placeholders().append("%player%", sender.getName()).append("%target%", offlinePlayer.getName()).append("%transaction%", String.valueOf(value)), true);
            } else {
                if (Vault.isHooked()) {
                    if (Vault.getEconomy().has(offlinePlayer, value)) {
                        Vault.getEconomy().withdrawPlayer(offlinePlayer, value);
                        userData.setBounty(userData.getBounty() + value);
                        MessagesUtil.BOUNTY_PLAYER_SET.msgAll(userData.user().placeholders().append("%player%", sender.getName()).append("%target%", offlinePlayer.getName()).append("%transaction%", String.valueOf(value)), true);
                    } else {
                        MessagesUtil.TRANSACTION_ERROR.msg(sender, ObjectMap.newHashObjectMap().append("%player%", sender.getName()).append("%transaction%", String.valueOf(value)), true);
                    }
                }
            }
        }
        return true;
    }
}
