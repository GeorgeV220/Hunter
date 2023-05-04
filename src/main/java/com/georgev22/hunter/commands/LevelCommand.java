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

@CommandAlias("level")
public class LevelCommand extends BaseCommand {

    @Default
    @Description("{@@commands.descriptions.level}")
    @CommandCompletion("@players")
    @CommandPermission("hunter.level")
    public void execute(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
        if (args.length == 0) {
            if (!commandIssuer.isPlayer()) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser((Player) commandIssuer.getIssuer());
            MessagesUtil.LEVEL_COMMAND.msg(
                    commandIssuer.getIssuer(),
                    userData.user().placeholders(),
                    true);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserData userData = UserData.getUser(target);
        MessagesUtil.LEVEL_COMMAND_OTHER.msg(
                commandIssuer.getIssuer(),
                userData.user().placeholders(),
                true);
    }

}
