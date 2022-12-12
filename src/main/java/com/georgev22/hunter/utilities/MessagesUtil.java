package com.georgev22.hunter.utilities;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.minecraft.configmanager.CFG;
import com.georgev22.library.minecraft.xseries.messages.Titles;
import com.georgev22.library.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum MessagesUtil {

    NO_PERMISSION("Messages.No Permission", "&c&l(!)&c You do not have the correct permissions to do this!"),

    ONLY_PLAYER_COMMAND("Messages.Only-Player-Command", "&c&l(!)&c Only players can run this command!"),

    PLAYER_NOT_FOUND("Messages.Player not found", "&c&l(!)&c Player not found!"),

    LEVEL_UP("Messages.Level Up", "&a&l(!)&a Player %player% reached level: %level%"),

    KILLSTREAK("Messages.Killstreak", "&a&l(!)&a Player %player% reached a killstreak of %killstreak%!"),

    KILLSTREAK_COMMAND("Messages.Killstreak command", "&aYou currently have a killstreak of %killstreak%", "&aYou currently have %kills% of total kills", "&aYou currently have %experience% experience", "&aYour level is %level%"),

    KILLSTREAK_COMMAND_OTHER("Messages.Killstreak command other", "&%player% currently have a killstreak of %killstreak%", "&a%player% currently have %kills% of total kills", "&a%player% currently have %experience% experience", "&a%player% level is %level%"),

    LEVEL_COMMAND("Messages.Level command", "&aYour level is %level%"),

    LEVEL_COMMAND_OTHER("Messages.Level command other", "&a%player% level is %level%"),

    TITLE_LEVEL_UP("Title.Level UP", "&aYou have reached level", "%level%"),

    ITEM_ON_COOLDOWN("Messages.Item on cooldown", "&c&l(!)&c Please wait %seconds%s in order to use this item(%item%) again."),

    PRESTIGE("Messages.Prestige", "&a&l(!)&a The prestige were successfully completed!"),

    TRANSACTION_WITHDRAW_SUCCESS("Messages.Transaction success withdraw", "&a&l(!)&a Successfully withdraw %transaction%"),

    TRANSACTION_DEPOSIT_SUCCESS("Messages.Transaction success deposit", "&a&l(!)&a Successfully deposit %transaction%"),

    TRANSACTION_ERROR("Messages.Transaction error", "&c&l(!)&c Insufficient funds (%transaction%)!"),

    TRANSACTION_FULL_INVENTORY("Messages.Transaction full inventory", "&c&l(!)&c Your inventory is full!"),

    TRANSACTION_ERROR_NEGATIVE("Messages.Transaction error negative", "&a&l(!)&c Negative numbers cannot be used!"),

    BOUNTY_PLAYER("Messages.Bounty player", "&a&l(!)&a You currently have %bounty% bounty on your head!"),

    BOUNTY_PLAYER_OTHER("Messages.Bounty player other", "&a&l(!)&a Player %player% have a %bounty% bounty on his head!"),

    BOUNTY_PLAYER_COLLECT("Messages.Bounty player collect", "&a&l(!)&a Player %player% collected %target% bounty (%bounty%)"),

    BOUNTY_PLAYER_SET("Messages.Bounty player set", "&a&l(!)&a %player% put a bounty on %target% head. (%transaction%/%bounty%)"),
    ;

    /**
     * @see #getMessages()
     */
    private String[] messages;
    private final String path;

    MessagesUtil(final String path, final String... messages) {
        this.messages = messages;
        this.path = path;
    }

    /**
     * @return boolean - Whether the messages array contains more than 1
     * element. If true, it's more than 1 message/string.
     */
    private boolean isMultiLined() {
        return this.messages.length > 1;
    }

    /**
     * @param cfg CFG instance
     */
    public static void repairPaths(final CFG cfg) {

        boolean changed = false;

        for (MessagesUtil enumMessage : MessagesUtil.values()) {

            /* Does our file contain our path? */
            if (cfg.getFileConfiguration().contains(enumMessage.getPath())) {
                /* It does! Let's set our message to be our path. */
                setPathToMessage(cfg, enumMessage);
                continue;
            }

            /* Since the path doesn't exist, let's set our default message to that path. */
            setMessageToPath(cfg, enumMessage);
            if (!changed) {
                changed = true;
            }

        }
        /* Save the custom yaml file. */
        if (changed) {
            cfg.saveFile();
        }
    }

    /**
     * Sets a message from the MessagesX enum to the file.
     *
     * @param cfg         CFG instance
     * @param enumMessage Message
     */
    private static void setMessageToPath(final CFG cfg, final @NotNull MessagesUtil enumMessage) {
        /* Is our message multilined? */
        if (enumMessage.isMultiLined()) {
            /* Set our message (array) to the path. */
            cfg.getFileConfiguration().set(enumMessage.getPath(), enumMessage.getMessages());
        } else {
            /* Set our message (string) to the path. */
            cfg.getFileConfiguration().set(enumMessage.getPath(), enumMessage.getMessages()[0]);
        }
    }

    /**
     * Sets the current MessagesX messages to a string/list retrieved from the
     * messages file.
     *
     * @param cfg         CFG instance
     * @param enumMessage Message
     */
    private static void setPathToMessage(final @NotNull CFG cfg, final @NotNull MessagesUtil enumMessage) {
        /* Is our path a list? */
        if (BukkitMinecraftUtils.isList(cfg.getFileConfiguration(), enumMessage.getPath())) {
            /* Set our default message to be the path's message. */
            enumMessage.setMessages(
                    cfg.getFileConfiguration().getStringList(enumMessage.getPath()).toArray(new String[0]));
        } else {
            /* Set our default message to be the path's message. */
            enumMessage.setMessages(cfg.getFileConfiguration().getString(enumMessage.getPath()));
        }
    }

    /**
     * @return the path - The path of the enum in the file.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @return the messages - The messages array that contains all strings.
     */
    public String[] getMessages() {
        return this.messages;
    }

    public @NotNull String getMessagesToString() {
        StringBuilder sb = new StringBuilder();
        for (String message : this.messages) {
            sb.append(message).append("\n");
        }
        return sb.toString();
    }

    /**
     * Sets the current messages to a different string array.
     *
     * @param messages The messages array that contains all strings.
     */
    public void setMessages(final String[] messages) {
        this.messages = messages;
    }

    /**
     * Sets the string message to a different string assuming that the array has
     * only 1 element.
     *
     * @param messages The message
     */
    public void setMessages(final String messages) {
        this.messages[0] = messages;
    }

    /**
     * @param target Message target
     * @see #msg(CommandSender, Map, boolean)
     */
    public void msg(final CommandSender target) {
        msg(target, new HashObjectMap<>(), false);
    }

    /**
     * Sends a translated message to a target commandsender with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     *
     * @param target     Message target
     * @param map        The Map with the placeholders
     * @param ignoreCase If you want to ignore case
     */
    public void msg(final CommandSender target, final Map<String, String> map, final boolean ignoreCase) {
        if (this.isMultiLined()) {
            BukkitMinecraftUtils.msg(target, this.getMessages(), map, ignoreCase);
        } else {
            BukkitMinecraftUtils.msg(target, this.getMessages()[0], map, ignoreCase);
        }
    }

    /**
     * Sends a translated title message to a target Player
     *
     * @param target Message target
     */
    public void title(final Player target) {
        title(target, 10, 20, 10, new HashObjectMap<>(), false);
    }

    /**
     * Sends a translated title message to a target Player with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     *
     * @param target     Message target
     * @param map        The Map with the placeholders
     * @param ignoreCase If you want to ignore case
     */
    public void title(final Player target, int fadeIn, int stay, int fadeOut,
                      final Map<String, String> map, final boolean ignoreCase) {
        if (this.isMultiLined()) {
            Titles.sendTitle(
                    target,
                    fadeIn,
                    stay,
                    fadeOut,
                    BukkitMinecraftUtils.colorize(Utils.placeHolder(this.getMessages()[0], map, ignoreCase)),
                    BukkitMinecraftUtils.colorize(Utils.placeHolder(this.getMessages()[1], map, ignoreCase))
            );
        } else {
            Titles.sendTitle(
                    target,
                    fadeIn,
                    stay,
                    fadeOut,
                    BukkitMinecraftUtils.colorize(Utils.placeHolder(this.getMessages()[0], map, ignoreCase)),
                    ""
            );
        }
    }

    public void titleAll() {
        Bukkit.getOnlinePlayers().forEach(this::title);
    }

    public void titleAll(int fadeIn, int stay, int fadeOut, final Map<String, String> map, final boolean ignoreCase) {
        Bukkit.getOnlinePlayers().forEach(target -> title(target, fadeIn, stay, fadeOut, map, ignoreCase));
    }

    /**
     * Sends a translated message to a target commandsender with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     */
    public void msgAll() {
        Bukkit.getOnlinePlayers().forEach(this::msg);
    }

    /**
     * Sends a translated message to a target with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     *
     * @param map        The placeholders map
     * @param ignoreCase If you want to ignore case
     */
    public void msgAll(final Map<String, String> map, final boolean ignoreCase) {
        Bukkit.getOnlinePlayers().forEach(target -> msg(target, map, ignoreCase));
    }

}
