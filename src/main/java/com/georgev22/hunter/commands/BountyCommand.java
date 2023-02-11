package com.georgev22.hunter.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.player.UserData;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.utilities.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@CommandAlias("bounty")
public class BountyCommand extends BaseCommand {


    @HelpCommand
    @Subcommand("help")
    @CommandAlias("bhelp")
    @Description("{@@commands.descriptions.bounty.help}")
    @CommandPermission("hunter.bounty.help")
    public void onHelp(final @NotNull CommandIssuer issuer) {
        for (String input : Arrays.asList(
                "&c&l(!)&c Commands &c&l(!)",
                "&6/bounty <player> [money]",
                "&c&l==============")) {
            issuer.sendMessage(LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacy('&').deserialize(input)));
        }
    }

    @Default
    @Description("{@@commands.descriptions.bounty.default}")
    @CommandCompletion("help|@players @range:9999")
    @CommandPermission("hunter.bounty")
    public void execute(final @NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
        if (args.length == 0) {
            if (!commandIssuer.isPlayer()) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                return;
            }
            MessagesUtil.BOUNTY_PLAYER.msg(commandIssuer.getIssuer(), UserData.getUser((Player) commandIssuer.getIssuer()).user().placeholders(), true);
            return;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        if (!offlinePlayer.isOnline() || !offlinePlayer.hasPlayedBefore()) {
            MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
            return;
        }
        if (args.length == 1) {
            MessagesUtil.BOUNTY_PLAYER_OTHER.msg(commandIssuer.getIssuer(), UserData.getUser(offlinePlayer).user().placeholders(), true);
            return;
        }
        if (!Utils.isDouble(args[1])) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Double)");
            return;
        }
        double value = Double.parseDouble(args[1]);
        if (value <= 0) {
            MessagesUtil.TRANSACTION_ERROR_NEGATIVE.msg(commandIssuer.getIssuer(), new HashObjectMap<String, String>().append("%player%", ((CommandSender) commandIssuer.getIssuer()).getName()).append("%transaction%", String.valueOf(value)), true);
            return;
        }
        UserData userData = UserData.getUser(offlinePlayer);
        if (!commandIssuer.isPlayer()) {
            userData.setBounty(userData.getBounty() + value);
            MessagesUtil.BOUNTY_PLAYER_SET.msgAll(userData.user().placeholders().append("%player%", ((CommandSender) commandIssuer.getIssuer()).getName()).append("%target%", offlinePlayer.getName()).append("%transaction%", String.valueOf(value)), true);
        } else {
            if (Vault.isHooked()) {
                if (Vault.getEconomy().has((OfflinePlayer) commandIssuer.getIssuer(), value)) {
                    Vault.getEconomy().withdrawPlayer((OfflinePlayer) commandIssuer.getIssuer(), value);
                    userData.setBounty(userData.getBounty() + value);
                    MessagesUtil.BOUNTY_PLAYER_SET.msgAll(userData.user().placeholders().append("%player%", ((CommandSender) commandIssuer.getIssuer()).getName()).append("%target%", offlinePlayer.getName()).append("%transaction%", String.valueOf(value)), true);
                } else {
                    MessagesUtil.TRANSACTION_ERROR.msg(commandIssuer.getIssuer(), new HashObjectMap<String, String>().append("%player%", ((CommandSender) commandIssuer.getIssuer()).getName()).append("%transaction%", String.valueOf(value)), true);
                }
            }
        }
    }
}
