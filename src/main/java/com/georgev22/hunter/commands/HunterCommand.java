package com.georgev22.hunter.commands;

import com.georgev22.api.inventory.ItemBuilder;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.hunter.Main;
import com.georgev22.hunter.hooks.HolographicDisplays;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import com.georgev22.hunter.utilities.player.UserData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.georgev22.api.utilities.Utils.*;

public class HunterCommand extends BukkitCommand {

    private final Main mainPlugin = Main.getInstance();

    public HunterCommand() {
        super("hunter");
        this.description = "hunter main command";
        this.usageMessage = "/hunter";
        this.setPermission("hunter.main");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();
            MinecraftUtils.msg(sender, "&a&l(!)&a Plugin configs successfully reloaded (Some settings will take effect after server restart)");
            return true;
        }
        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length == 1) {
                MinecraftUtils.msg(sender, "&c&l(!)&c /hunter clear <player>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UserData userData = UserData.getUser(target.getUniqueId());

            if (userData.playerExists()) {
                try {
                    userData.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MinecraftUtils.msg(sender, "&c&l(!)&c You cleared player " + target.getName());
            } else {
                MinecraftUtils.msg(sender, "&c&l(!)&c Player " + target.getName() + " doesn't exist");
            }
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                MinecraftUtils.msg(sender, "&c&l(!)&c /hunter set <player> <data> <value>");
                MinecraftUtils.msg(sender, "&c&l(!)&c Data: kills killstreak prestige levels multiplier experience");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UserData userData = UserData.getUser(target.getUniqueId());
            if (args[2].equalsIgnoreCase("kills")) {
                userData.setKills(Integer.parseInt(args[3]));
                MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " kills to " + args[3]);
            } else if (args[2].equalsIgnoreCase("killstreak")) {
                userData.setKillstreak(Integer.parseInt(args[3]));
                MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " killstreak to " + args[3]);
            } else if (args[2].equalsIgnoreCase("prestige")) {
                userData.setPrestige(Integer.parseInt(args[3]));
                MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " prestige to " + args[3]);
            } else if (args[2].equalsIgnoreCase("levels")) {
                userData.setLevel(Integer.parseInt(args[3]));
                MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " levels to " + args[3]);
            } else if (args[2].equalsIgnoreCase("multiplier")) {
                userData.setMultiplier(Double.parseDouble(args[3]));
                MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " multiplier to " + args[3]);
            } else if (args[2].equalsIgnoreCase("experience")) {
                userData.setExperience(Double.parseDouble(args[3]));
                MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " experience to " + args[3]);
            } else {
                MinecraftUtils.msg(sender, "&c&l(!)&c /hunter set <player> <data> <value>");
                MinecraftUtils.msg(sender, "&c&l(!)&c Data: kills killstreak prestige levels multiplier experience");
            }
            UserData.getAllUsersMap().replace(target.getUniqueId(), userData.user());
            userData.save(true, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            return true;
        } else if (args[0].equalsIgnoreCase("copy")) {
            if (args.length < 2) {
                MinecraftUtils.msg(sender, "&c&l(!)&c /hunter copy <config> <newConfig>");
                return true;
            }
            String originalFileName = args[1];
            String newFileName = args[2];
            if (Files.exists(new File(mainPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + originalFileName).toPath())) {
                if (Files.exists(new File(mainPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + newFileName).toPath())) {
                    MinecraftUtils.msg(sender, "&c&l(!)&c Target file already exists.");
                    return true;
                }
                try {
                    Files.copy(new File(mainPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + originalFileName).toPath(),
                            new File(mainPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + newFileName).toPath());
                    MinecraftUtils.msg(sender, "&a&l(!)&a Successfully copied " + originalFileName + " to " + newFileName);
                } catch (IOException e) {
                    MinecraftUtils.msg(sender, "&c&l(!)&c An error has occurred, check the console for logs.");
                    e.printStackTrace();
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("transaction")) {
            if (args.length < 4) {
                MinecraftUtils.msg(sender, "&c&l(!)&c /hunter transaction <player> <funds> <data> <values>");
                MinecraftUtils.msg(sender, "&c&l(!)&c Data: prestige sell buy");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                //TODO PLAYER NOT FOUND ERROR
                return true;
            }
            UserData userData = UserData.getUser(target);
            FileManager fileManager = FileManager.getInstance();
            if (!userData.playerExists()) {
                //TODO PLAYER NOT FOUND ERROR
                return true;
            }
            double transaction = Double.parseDouble(args[2]);
            if (Vault.isHooked()) {
                if (args[3].equalsIgnoreCase("prestige")) {
                    if (args.length < 7) {
                        MinecraftUtils.msg(sender, "&c&l(!)&c /hunter transaction <player> <funds> prestige <prestige> <multiplier> <global>");
                        return true;
                    }
                    if (Vault.getEconomy().has(target, transaction)) {
                        userData.setPrestige(userData.getPrestige() + Integer.parseInt(args[4])).setMultiplier(userData.getMultiplier() + Double.parseDouble(args[5]));
                        MessagesUtil.PRESTIGE.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                        Vault.getEconomy().withdrawPlayer(target, transaction);
                        if (Boolean.parseBoolean(args[6])) {
                            MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                        } else {
                            MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                        }
                    } else {
                        MessagesUtil.TRANSACTION_ERROR.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)), true);
                    }
                } else if (args[3].equalsIgnoreCase("sell")) {
                    if (args.length < 5) {
                        MinecraftUtils.msg(sender, "&c&l(!)&c /hunter transaction <player> <funds> sell <value>");
                        return true;
                    }
                    ItemBuilder itemBuilder = ItemBuilder.buildSimpleItemFromConfig(fileManager.getItems().getFileConfiguration(), "sell." + args[4]);
                    ItemStack itemStack = itemBuilder.build();
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
                                MessagesUtil.TRANSACTION_DEPOSIT_SUCCESS.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                            }
                            break;
                        }
                    }
                } else if (args[3].equalsIgnoreCase("buy")) {
                    if (args.length < 5) {
                        MinecraftUtils.msg(sender, "&c&l(!)&c /hunter transaction <player> <funds> buy <value>");
                        return true;
                    }
                    if (Vault.getEconomy().has(target, transaction)) {
                        if (target.getInventory().firstEmpty() != -1) {
                            ItemBuilder itemBuilder = ItemBuilder.buildSimpleItemFromConfig(fileManager.getItems().getFileConfiguration(), "buy." + args[4], userData.user().placeholders(), userData.user().placeholders());
                            ItemStack itemStack = itemBuilder.build();
                            target.getInventory().addItem(itemStack);
                            Vault.getEconomy().withdrawPlayer(target, transaction);
                            MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)).append(userData.user().placeholders()), true);
                        } else {
                            MessagesUtil.TRANSACTION_FULL_INVENTORY.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)), true);
                        }
                    } else {
                        MessagesUtil.TRANSACTION_ERROR.msg(target, ObjectMap.newHashObjectMap().append("%player%", target.getName()).append("%transaction%", String.valueOf(transaction)), true);
                    }
                }
            } else {
                MinecraftUtils.msg(sender, "&c&l(!)&c Vault is not hooked!");
            }
        } else if (args[0].equalsIgnoreCase("hologram")) {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHolographicDisplays is not enabled!");
                return true;
            }

            if (args.length == 1) {
                MinecraftUtils.msg(sender, "&c&l(!) &cNot enough arguments!");
                return true;
            }

            if (args[1].equalsIgnoreCase("create")) {
                if (!(sender instanceof Player)) {
                    MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                    return true;
                }
                Player player = (Player) sender;
                if (args.length < 4) {
                    MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hunter hologram create <hologramName> <type>");
                    return true;
                }

                if (HolographicDisplays.hologramExists(args[2])) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
                    return true;
                }

                if (mainPlugin.getConfig().get("Holograms." + args[3]) == null) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                    return true;
                }

                HolographicDisplays.show(HolographicDisplays.updateHologram(HolographicDisplays.create(
                                        args[2],
                                        player.getLocation(),
                                        args[3], true),
                                mainPlugin.getConfig().getStringList("Holograms." + args[2]), HolographicDisplays.getPlaceholderMap()),
                        player);

                HolographicDisplays.getPlaceholderMap().clear();

                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[2] + " with type " + args[3] + " successfully created!");

            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 2) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cUsage: /hunter hologram remove <hologramName>");
                    return true;
                }

                if (!HolographicDisplays.hologramExists(args[2])) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                    return true;
                }

                HolographicDisplays.remove(args[2], true);

                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[2] + " successfully removed!");
            }
        } else if (args[0].equalsIgnoreCase("help")) {
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            MinecraftUtils.msg(sender, "&6/hunter clear <player>");
            MinecraftUtils.msg(sender, "&6/hunter set <player> <data> <value>");
            MinecraftUtils.msg(sender, "&c&l(!)&c /hunter transaction <player> <funds> <data> <values>");
            MinecraftUtils.msg(sender, "&6/hunter reload");
            MinecraftUtils.msg(sender, "&6/hunter help");
            MinecraftUtils.msg(sender, "&c&l==============");
        } else {
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            MinecraftUtils.msg(sender, "&6/hunter clear <player>");
            MinecraftUtils.msg(sender, "&6/hunter set <player> <data> <value>");
            MinecraftUtils.msg(sender, "&c&l(!)&c /hunter transaction <player> <funds> <data> <values>");
            MinecraftUtils.msg(sender, "&6/hunter reload");
            MinecraftUtils.msg(sender, "&6/hunter help");
            MinecraftUtils.msg(sender, "&c&l==============");
            return true;
        }
        return true;
    }
}
