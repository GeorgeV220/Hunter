package com.georgev22.hunter.commands;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.utilities.Utils;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.player.UserData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LevelCommand extends BukkitCommand {

    public LevelCommand() {
        super("level");
        this.description = "Level command";
        this.usageMessage = "/level";
        this.setPermission("level.use");
        this.setPermissionMessage(BukkitMinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return true;
            }
            UserData userData = UserData.getUser((Player) sender);
            MessagesUtil.LEVEL_COMMAND.msg(
                    sender,
                    new HashObjectMap<String, String>()
                            .append("%player%", sender.getName())
                            .append("%kills%", String.valueOf(userData.getKills()))
                            .append("%killstreak%", String.valueOf(userData.getKillStreak()))
                            .append("%level%", String.valueOf(userData.getLevel()))
                            .append("%experience%", String.valueOf(userData.getExperience()))
                            .append("%level_roman%", Utils.toRoman(userData.getLevel())),
                    true);
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserData userData = UserData.getUser(target);
        MessagesUtil.LEVEL_COMMAND_OTHER.msg(
                sender,
                new HashObjectMap<String, String>()
                        .append("%player%", target.getName())
                        .append("%kills%", String.valueOf(userData.getKills()))
                        .append("%killstreak%", String.valueOf(userData.getKillStreak()))
                        .append("%level%", String.valueOf(userData.getLevel()))
                        .append("%experience%", String.valueOf(userData.getExperience()))
                        .append("%level_roman%", Utils.toRoman(userData.getLevel())),
                true);
        return true;
    }
}
