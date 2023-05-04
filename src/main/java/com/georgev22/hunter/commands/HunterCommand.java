package com.georgev22.hunter.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.georgev22.hunter.HunterPlugin;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import com.georgev22.hunter.utilities.player.UserData;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.minecraft.inventory.ItemBuilder;
import com.georgev22.library.utilities.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static com.georgev22.library.utilities.Utils.Callback;

@CommandAlias("hunter")
public class HunterCommand extends BaseCommand {

    private final HunterPlugin hunterPlugin = HunterPlugin.getInstance();

    @Default
    @HelpCommand
    @Subcommand("help")
    @CommandAlias("hhelp")
    @Description("{@@commands.descriptions.hunter.help}")
    @CommandPermission("hunter.help")
    public void onHelp(@NotNull CommandIssuer commandIssuer) {
        for (String input : Arrays.asList(
                "&c&l(!)&c Commands &c&l(!)",
                "&6/hunter clear <player>",
                "&6/hunter set <player> <data> <value>",
                "&6/hunter transaction <player> <funds> <data> <values>",
                "&6/hunter hologram <data> <values>",
                "&6/hunter reload",
                "&6/hunter help",
                "&c&l==============")) {
            commandIssuer.sendMessage(LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacy('&').deserialize(input)));
        }

    }

    @Subcommand("clear")
    @CommandAlias("hclear")
    @Description("{@@commands.descriptions.hunter.clear}")
    @CommandPermission("hunter.clear")
    public void clear(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
        if (args.length == 0) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter clear <player>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserData userData = UserData.getUser(target.getUniqueId());

        if (userData.playerExists()) {
            try {
                userData.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c You cleared player " + target.getName());
        } else {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Player " + target.getName() + " doesn't exist");
        }
    }

    @Subcommand("reload")
    @CommandAlias("hreload")
    @Description("{@@commands.descriptions.hunter.reload}")
    @CommandPermission("hunter.reload")
    public void reload(@NotNull CommandIssuer commandIssuer) {
        HunterPlugin.getInstance().reloadConfig();
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Plugin configs successfully reloaded (Some settings will take effect after server restart)");
    }

    @Subcommand("copy")
    @CommandAlias("hcopy")
    @Description("{@@commands.descriptions.hunter.copy}")
    @CommandPermission("hunter.copy")
    public void copy(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
        if (args.length <= 1) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter copy <config> <newConfig>");
            return;
        }
        String originalFileName = args[0];
        String newFileName = args[1];
        if (Files.exists(new File(hunterPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + originalFileName).toPath())) {
            if (Files.exists(new File(hunterPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + newFileName).toPath())) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Target file already exists.");
                return;
            }
            try {
                Files.copy(new File(hunterPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + originalFileName).toPath(),
                        new File(hunterPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + newFileName).toPath());
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Successfully copied " + originalFileName + " to " + newFileName);
            } catch (IOException e) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c An error has occurred, check the console for logs.");
                e.printStackTrace();
            }
        }
    }

    @Subcommand("set")
    @CommandAlias("hset")
    @Description("{@@commands.descriptions.hunter.set.default}")
    @CommandPermission("hunter.set")
    public class Set extends BaseCommand {

        @Subcommand("kills")
        @CommandAlias("hsetkills")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.set.kills}")
        @CommandPermission("hunter.set.kills")
        public void kills(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 1) {
                onHelp(commandIssuer);
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() | !target.isOnline()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target.getUniqueId());
            if (!Utils.isInt(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Integer)");
                return;
            }
            userData.setKills(Integer.parseInt(args[1]));
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " kills to " + args[1]);
            save(target, userData);
        }

        @Subcommand("killstreak")
        @CommandAlias("hsetkillstreak")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.set.killstreak}")
        @CommandPermission("hunter.set.killstreak")
        public void killstreak(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 1) {
                onHelp(commandIssuer);
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() | !target.isOnline()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target.getUniqueId());
            if (!Utils.isInt(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Integer)");
                return;
            }
            userData.setKillstreak(Integer.parseInt(args[1]));
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " killstreak to " + args[1]);
            save(target, userData);
        }

        @Subcommand("prestige")
        @CommandAlias("hsetprestige")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.set.prestige}")
        @CommandPermission("hunter.set.prestige")
        public void prestige(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 1) {
                onHelp(commandIssuer);
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() | !target.isOnline()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target.getUniqueId());
            if (!Utils.isInt(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Integer)");
                return;
            }
            userData.setPrestige(Integer.parseInt(args[1]));
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " prestige to " + args[1]);
            save(target, userData);
        }

        @Subcommand("levels")
        @CommandAlias("hsetlevels")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.set.levels}")
        @CommandPermission("hunter.set.levels")
        public void levels(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 1) {
                onHelp(commandIssuer);
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() | !target.isOnline()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target.getUniqueId());
            if (!Utils.isInt(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Integer)");
                return;
            }
            userData.setLevel(Integer.parseInt(args[1]));
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " levels to " + args[1]);
            save(target, userData);
        }

        @Subcommand("multiplier")
        @CommandAlias("hsetmultiplier")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.set.multiplier}")
        @CommandPermission("hunter.set.multiplier")
        public void multiplier(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 1) {
                onHelp(commandIssuer);
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() | !target.isOnline()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target.getUniqueId());
            if (!Utils.isDouble(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Double)");
                return;
            }
            userData.setMultiplier(Double.parseDouble(args[1]));
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " multiplier to " + args[1]);
            save(target, userData);
        }

        @Subcommand("experience")
        @CommandAlias("hsetexperience")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.set.experience}")
        @CommandPermission("hunter.set.experience")
        public void experience(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 1) {
                onHelp(commandIssuer);
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() | !target.isOnline()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target.getUniqueId());
            if (!Utils.isDouble(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Double)");
                return;
            }
            userData.setExperience(Double.parseDouble(args[1]));
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " experience to " + args[1]);
            save(target, userData);
        }

        private void save(OfflinePlayer target, UserData userData) {
            UserData.getAllUsersMap().replace(target.getUniqueId(), userData.user());
            userData.save(true, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    return true;
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    return onFailure();
                }

                @Override
                public Boolean onFailure() {
                    return false;
                }
            });
        }

        @Default
        @HelpCommand
        @Subcommand("help")
        @CommandAlias("hsethelp")
        @Description("{@@commands.descriptions.hunter.set.help}")
        @CommandPermission("hunter.set.help")
        public void onHelp(@NotNull CommandIssuer commandIssuer) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter set <data> <player> <value>");
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Data: kills killstreak prestige levels multiplier experience");
        }

    }

    @Subcommand("transaction")
    @CommandAlias("htransaction")
    @Description("{@@commands.descriptions.hunter.transaction.default}")
    @CommandPermission("hunter.transaction")
    public class Transaction extends BaseCommand {

        @Subcommand("prestige")
        @CommandAlias("htransactionprestige")
        @CommandCompletion("@players @range:9999 @range:9999 @range:9999 true|false")
        @Description("{@@commands.descriptions.hunter.transaction.prestige}")
        @CommandPermission("hunter.transaction.prestige")
        public void prestige(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length < 4) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter transaction prestige <player> <funds> <prestige> <multiplier> <global>");
                return;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target);
            FileManager fileManager = FileManager.getInstance();
            if (!userData.playerExists()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            if (!Utils.isDouble(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Double)");
                return;
            }
            double transaction = Double.parseDouble(args[1]);
            if (Vault.isHooked()) {
                if (Vault.getEconomy().has(target, transaction)) {
                    userData.setPrestige(userData.getPrestige() + Integer.parseInt(args[2])).setMultiplier(userData.getMultiplier() + Double.parseDouble(args[3]));
                    MessagesUtil.PRESTIGE.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                    Vault.getEconomy().withdrawPlayer(target, transaction);
                    if (Boolean.parseBoolean(args[4])) {
                        MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msgAll(new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                    } else {
                        MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                    }
                } else {
                    MessagesUtil.TRANSACTION_ERROR.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)), true);
                }
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Vault is not hooked!");
            }
        }

        @Subcommand("sell")
        @CommandAlias("htransactionsell")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.transaction.sell}")
        @CommandPermission("hunter.transaction.sell")
        public void sell(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 2) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter transaction sell <player> <funds> <value>");
                return;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target);
            FileManager fileManager = FileManager.getInstance();
            if (!userData.playerExists()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            if (!Utils.isDouble(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Double)");
                return;
            }
            double transaction = Double.parseDouble(args[1]);
            if (Vault.isHooked()) {
                ItemBuilder itemBuilder = ItemBuilder.buildSimpleItemFromConfig(fileManager.getItems().getFileConfiguration(), "sell." + args[2]);
                ItemStack itemStack = itemBuilder.build();
                if (itemStack.getItemMeta().getDisplayName().equals(BukkitMinecraftUtils.colorize("&c&l&nInvalid path!!"))) {
                    BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Invalid path!!");
                    return;
                }
                for (ItemStack i : target.getInventory().getContents()) {
                    if (i == null || i.equals(new ItemStack(Material.AIR))) {
                        continue;
                    }
                    if (i.getType().equals(itemStack.getType())) {
                        if (i.getAmount() >= itemStack.getAmount()) {
                            if (i.getAmount() == 1) {
                                target.getInventory().removeItem(i);
                            } else {
                                i.setAmount(i.getAmount() - 1);
                            }
                            Vault.getEconomy().depositPlayer(target, transaction);
                            MessagesUtil.TRANSACTION_DEPOSIT_SUCCESS.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                        }
                        break;
                    }
                }
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Vault is not hooked!");
            }
        }

        @Subcommand("buy")
        @CommandAlias("htransactionbuy")
        @CommandCompletion("@players @range:9999")
        @Description("{@@commands.descriptions.hunter.transaction.buy}")
        @CommandPermission("hunter.transaction.buy")
        public void buy(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (args.length <= 2) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter transaction buy <player> <funds> <value>");
                return;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            UserData userData = UserData.getUser(target);
            FileManager fileManager = FileManager.getInstance();
            if (!userData.playerExists()) {
                MessagesUtil.PLAYER_NOT_FOUND.msg(commandIssuer.getIssuer());
                return;
            }
            if (!Utils.isDouble(args[1])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c " + args[1] + " is not a number (Double)");
                return;
            }
            double transaction = Double.parseDouble(args[1]);
            if (Vault.isHooked()) {
                if (Vault.getEconomy().has(target, transaction)) {
                    if (target.getInventory().firstEmpty() != -1) {
                        ItemBuilder itemBuilder = ItemBuilder.buildSimpleItemFromConfig(fileManager.getItems().getFileConfiguration(), "buy." + args[2], userData.user().placeholders(), userData.user().placeholders());
                        ItemStack itemStack = itemBuilder.build();
                        if (itemStack.getItemMeta().getDisplayName().equals(BukkitMinecraftUtils.colorize("&c&l&nInvalid path!!"))) {
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Invalid path!!");
                            return;
                        }
                        target.getInventory().addItem(itemStack);
                        Vault.getEconomy().withdrawPlayer(target, transaction);
                        MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                    } else {
                        MessagesUtil.TRANSACTION_FULL_INVENTORY.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)), true);
                    }
                } else {
                    MessagesUtil.TRANSACTION_ERROR.msg(target, new HashObjectMap<String, String>().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)), true);
                }
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Vault is not hooked!");
            }
        }

        @Default
        @HelpCommand
        @Subcommand("help")
        @CommandAlias("htransactionhelp")
        @Description("{@@commands.descriptions.hunter.transaction.help}")
        @CommandPermission("hunter.transaction.help")
        public void onHelp(@NotNull CommandIssuer commandIssuer) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter transaction <data> <player> <funds> <values>");
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Data: prestige sell buy");
        }

    }

    @Subcommand("hologram")
    @CommandAlias("hhologram")
    @Description("{@@commands.descriptions.hunter.hologram.default}")
    @CommandPermission("hunter.hologram")
    public class Hologram extends BaseCommand {

        @Subcommand("create")
        @CommandAlias("hhcreate")
        @Description("{@@commands.descriptions.hunter.hologram.create}")
        @CommandPermission("hunter.hologram.create")
        public void create(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c HolographicDisplays is not enabled!");
                return;
            }
            if (!commandIssuer.isPlayer()) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                return;
            }
            if (args.length < 2) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Usage: /hunter hologram create <hologramName> <type>");
                return;
            }

            if (hunterPlugin.getHolograms().hologramExists(args[0])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Hologram already exists!");
                return;
            }

            if (hunterPlugin.getConfig().get("Holograms." + args[1]) == null) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Hologram type doesn't exists!");
                return;
            }

            hunterPlugin.getHolograms().show(hunterPlugin.getHolograms().updateHologram(hunterPlugin.getHolograms()
                                    .create(
                                            args[0],
                                            ((Player) commandIssuer.getIssuer()).getLocation(),
                                            args[1], true),
                            hunterPlugin.getConfig().getStringList("Holograms." + args[1]),
                            hunterPlugin.getHolograms().getPlaceholderMap()),
                    commandIssuer.getIssuer());

            hunterPlugin.getHolograms().getPlaceholderMap().clear();

            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Hologram " + args[0] + " with type " + args[1] + " successfully created!");
        }

        @Subcommand("remove")
        @CommandAlias("hhremove")
        @Description("{@@commands.descriptions.hunter.hologram.remove}")
        @CommandPermission("hunter.hologram.remove")
        public void remove(@NotNull CommandIssuer commandIssuer, String @NotNull [] args) {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c HolographicDisplays is not enabled!");
                return;
            }
            if (args.length == 0) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Usage: /hunter hologram remove <hologramName>");
                return;
            }

            if (!hunterPlugin.getHolograms().hologramExists(args[0])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Hologram doesn't exists!");
                return;
            }

            hunterPlugin.getHolograms().remove(args[0], true);

            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Hologram " + args[0] + " successfully removed!");
        }

        @Default
        @HelpCommand
        @Subcommand("help")
        @CommandAlias("hhologramhelp")
        @Description("{@@commands.descriptions.hunter.hologram.help}")
        @CommandPermission("hunter.hologram.help")
        public void onHelp(@NotNull CommandIssuer commandIssuer) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /hunter hologram <data> <values>");
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Data: remove create");
        }

    }
}
