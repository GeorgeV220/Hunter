package com.georgev22.killstreak.commands;

import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.killstreak.Main;
import com.georgev22.killstreak.hooks.HolographicDisplays;
import com.georgev22.killstreak.utilities.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KillstreakMainCommand extends BukkitCommand {

    private final Main m = Main.getInstance();

    public KillstreakMainCommand() {
        super("killstreakmain");
        this.description = "Killstreak main command";
        this.usageMessage = "/killstreakmain";
        this.setPermission("killstreakmain.use");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();
            MinecraftUtils.msg(sender, "&a&l(!)&a Plugin configs successfully reloaded (Some settings will take effect after server restart)");
            return true;
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
                    MinecraftUtils.msg(player, "&c&l(!) &cUsage: /killstreakmain hologram create <hologramName> <type>");
                    return true;
                }

                if (HolographicDisplays.hologramExists(args[2])) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
                    return true;
                }

                if (m.getConfig().get("Holograms." + args[3]) == null) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                    return true;
                }

                HolographicDisplays.show(HolographicDisplays.updateHologram(HolographicDisplays.create(
                                        args[2],
                                        player.getLocation(),
                                        args[3], true),
                                m.getConfig().getStringList("Holograms." + args[2]), HolographicDisplays.getPlaceholderMap()),
                        player);

                HolographicDisplays.getPlaceholderMap().clear();

                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[2] + " with type " + args[3] + " successfully created!");

            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 2) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cUsage: /killstreakmain hologram remove <hologramName>");
                    return true;
                }

                if (!HolographicDisplays.hologramExists(args[2])) {
                    MinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                    return true;
                }

                HolographicDisplays.remove(args[2], true);

                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[2] + " successfully removed!");
            }
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&cSub command does not exist!");
        }
        return true;
    }
}
