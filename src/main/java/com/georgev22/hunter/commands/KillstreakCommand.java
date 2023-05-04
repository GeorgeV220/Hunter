package com.georgev22.hunter.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.player.UserData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("killstreak")
public class KillstreakCommand extends BaseCommand {

    @Default
    @Description("{@@commands.descriptions.killstreak}")
    @CommandCompletion("@players")
    @CommandPermission("hunter.killstreak")
    public void execute(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
        if (args.length == 0) {
            if (!commandIssuer.isPlayer()) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser((Player) commandIssuer.getIssuer());
            MessagesUtil.KILLSTREAK_COMMAND.msg(
                    commandIssuer.getIssuer(),
                    userData.user().placeholders(),
                    true);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserData userData = UserData.getUser(target);
        MessagesUtil.KILLSTREAK_COMMAND_OTHER.msg(
                commandIssuer.getIssuer(),
                userData.user().placeholders(),
                true);
    }

}
