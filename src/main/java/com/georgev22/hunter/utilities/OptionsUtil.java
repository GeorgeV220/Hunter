package com.georgev22.hunter.utilities;

import com.georgev22.api.minecraft.colors.Color;
import com.georgev22.api.minecraft.inventory.ItemBuilder;
import com.georgev22.api.minecraft.xseries.XMaterial;
import com.georgev22.hunter.Main;
import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.georgev22.api.utilities.Utils.Assertions.notNull;

public enum OptionsUtil {

    DEBUG("debug", false, Optional.empty()),

    DEBUG_ERROR("show errors", false, Optional.empty()),

    COMMAND_KILLSTREAK("command.killstreak", true, Optional.empty()),

    COMMAND_HUNTER("command.hunter", true, Optional.of("command.killstreak main")),

    COMMAND_PRESTIGE("command.prestige", true, Optional.empty()),

    COMMAND_LEVEL("command.level", true, Optional.empty()),

    COMMAND_BOUNTY("command.bounty", true, Optional.empty()),

    DATABASE_HOST("database.SQL.host", "localhost", Optional.empty()),

    DATABASE_PORT("database.SQL.port", 3306, Optional.empty()),

    DATABASE_USER("database.SQL.user", "youruser", Optional.empty()),

    DATABASE_PASSWORD("database.SQL.password", "yourpassword", Optional.empty()),

    DATABASE_DATABASE("database.SQL.database", "hunter", Optional.empty()),

    DATABASE_TABLE_NAME("database.SQL.table name", "hunter_users", Optional.empty()),

    DATABASE_SQLITE("database.SQLite.file name", "hunter", Optional.empty()),

    DATABASE_MONGO_HOST("database.MongoDB.host", "localhost", Optional.empty()),

    DATABASE_MONGO_PORT("database.MongoDB.port", 27017, Optional.empty()),

    DATABASE_MONGO_USER("database.MongoDB.user", "youruser", Optional.empty()),

    DATABASE_MONGO_PASSWORD("database.MongoDB.password", "yourpassword", Optional.empty()),

    DATABASE_MONGO_DATABASE("database.MongoDB.database", "hunter", Optional.empty()),

    DATABASE_MONGO_COLLECTION("database.MongoDB.collection", "hunter_users", Optional.empty()),

    DATABASE_TYPE("database.type", "File", Optional.empty()),

    EXPERIMENTAL_FEATURES("experimental features", false, Optional.empty()),

    UPDATER("updater.enabled", true, Optional.of("updater")),

    UPDATER_DOWNLOAD("updater.download", false, Optional.empty()),

    UPDATER_RESTART("updater.restart", false, Optional.empty()),

    LEVELS_MESSAGE_RECEIVER("levels.message.receiver", "all", Optional.of("message.level up.receiver")),

    LEVELS_MESSAGE("levels.message.enabled", true, Optional.of("message.level up.enabled")),

    LEVELS_MESSAGE__EVERY("levels.message.every", 5, Optional.of("message.level up.every")),

    KILLSTREAK_MESSAGE_RECEIVER("killstreak.message.receiver", "all", Optional.of("message.killstreak.receiver")),

    KILLSTREAK_MESSAGE("killstreak.message.enabled", true, Optional.of("message.killstreak.enabled")),

    KILLSTREAK_MESSAGE_EVERY("killstreak.message.every", 10, Optional.of("message.killstreak.every")),

    LEVELS_TITLE_RECEIVER("levels.title.receiver", "player", Optional.of("title.level up.receiver")),

    LEVELS_TITLE("levels.title.enabled", true, Optional.of("title.level up.enabled")),

    LEVELS_TITLE_EVERY("levels.title.every", 5, Optional.of("title.level up.every")),

    LEVELS_TITLE_FADE_IN("levels.title.fade in", 10, Optional.of("title.level up.fade in")),

    LEVELS_TITLE__STAY("levels.title.stay", 20, Optional.of("title.level up.stay")),

    LEVELS_TITLE_FADE_OUT("levels.title.fade out", 10, Optional.of("title.level up.fade out")),

    LEVELS_REWARDS("levels.rewards", true, Optional.of("rewards.level up")),

    KILLSTREAK_REWARDS("killstreak.rewards", true, Optional.of("rewards.killstreak")),

    KILLS_REWARDS("kills.rewards", true, Optional.of("rewards.kills.enabled")),

    KILLS_REWARDS_CLOSEST("kills.closest", true, Optional.of("rewards.kills.closest")),

    LEVELS_DISCORD("levels.discord", false, Optional.of("discord.level up")),

    KILLS_DISCORD("kills.discord", false, Optional.of("discord.kills")),

    KILLSTREAK_DISCORD("killstreak.discord", false, Optional.of("discord.killstreak")),

    LEVELS_TOP("levels.top", 5, Optional.of("top.levels")),

    KILLS_TOP("kills.top", 5, Optional.of("top.kills")),

    KILLSTREAK_TOP("killstreak.top", 5, Optional.of("top.killstreak")),

    BOUNTY_ENABLED("bounty.enabled", false, Optional.empty()),

    BOUNTY_BASE("bounty.base", 300.0, Optional.empty()),

    BOUNTY_KILLSTREAK("bounty.killstreak", 10, Optional.empty()),

    BOUNTY_KILLSTREAK_ENABLED("bounty.killstreak enabled", true, Optional.empty()),

    BOUNTY_PERCENTAGE("bounty.percentage", 3.5, Optional.empty()),

    BOUNTY_PERCENTAGE_ENABLE("bounty.percentage enabled", true, Optional.empty()),

    ;
    private final String pathName;
    private final Object value;
    private final Optional<String>[] oldPaths;
    private static final Main mainPlugin = Main.getInstance();

    @SafeVarargs
    @Contract(pure = true)
    OptionsUtil(final String pathName, final Object value, Optional<String>... oldPaths) {
        this.pathName = pathName;
        this.value = value;
        this.oldPaths = oldPaths;
    }

    public boolean getBooleanValue() {
        return mainPlugin.getConfig().getBoolean(getPath(), Boolean.parseBoolean(String.valueOf(getDefaultValue())));
    }

    public Object getObjectValue() {
        return mainPlugin.getConfig().get(getPath(), getDefaultValue());
    }

    public String getStringValue() {
        return mainPlugin.getConfig().getString(getPath(), String.valueOf(getDefaultValue()));
    }

    public @NotNull
    Long getLongValue() {
        return mainPlugin.getConfig().getLong(getPath(), Long.parseLong(String.valueOf(getDefaultValue())));
    }

    public @NotNull
    Integer getIntValue() {
        return mainPlugin.getConfig().getInt(getPath(), Integer.parseInt(String.valueOf(getDefaultValue())));
    }

    public @NotNull
    Double getDoubleValue() {
        return mainPlugin.getConfig().getDouble(getPath(), Double.parseDouble(String.valueOf(getDefaultValue())));
    }

    public @NotNull
    List<String> getStringList() {
        return mainPlugin.getConfig().getStringList(getPath());
    }

    public ItemStack getItemStack(boolean isSavedAsItemStack) {
        if (isSavedAsItemStack) {
            return mainPlugin.getConfig().getItemStack(getPath(), (ItemStack) getDefaultValue());
        } else {
            if (mainPlugin.getConfig().get(getPath()) == null) {
                return (ItemStack) getDefaultValue();
            }
            ItemBuilder itemBuilder = new ItemBuilder(
                    notNull("Material", XMaterial.valueOf(mainPlugin.getConfig().getString(getPath() + ".item")).parseMaterial()))
                    .amount(mainPlugin.getConfig().getInt(getPath() + ".amount"))
                    .title(mainPlugin.getConfig().getString(getPath() + ".title"))
                    .lores(mainPlugin.getConfig().getStringList(getPath() + ".lores"))
                    .showAllAttributes(
                            mainPlugin.getConfig().getBoolean(getPath() + ".show all attributes"))
                    .glow(mainPlugin.getConfig().getBoolean(getPath() + ".glow"));
            return itemBuilder.build();
        }
    }

    /**
     * Converts and return a String List of color codes to a List of Color classes that represent the colors.
     *
     * @return a List of Color classes that represent the colors.
     */
    public @NotNull
    List<Color> getColors() {
        List<Color> colors = Lists.newArrayList();
        for (String stringColor : getStringList()) {
            colors.add(Color.from(stringColor));
        }

        return colors;
    }

    /**
     * Returns the path.
     *
     * @return the path.
     */
    public @NotNull
    String getPath() {
        if (getOldPaths().length > 0) {
            for (Optional<String> path : getOldPaths()) {
                if (mainPlugin.getConfig().get("Options." + getDefaultPath()) == null & path.isPresent()) {
                    if (mainPlugin.getConfig().get("Options." + path.get()) != null) {
                        return "Options." + path.get();
                    }
                } else {
                    return "Options." + getDefaultPath();
                }
            }
        }
        return "Options." + getDefaultPath();
    }

    /**
     * Returns the default path.
     *
     * @return the default path.
     */
    @Contract(pure = true)
    public @NotNull
    String getDefaultPath() {
        return this.pathName;
    }

    /**
     * Returns the old path if it exists.
     *
     * @return the old path if it exists.
     */
    public Optional<String>[] getOldPaths() {
        return oldPaths;
    }

    /**
     * Returns the default value if the path have no value.
     *
     * @return the default value if the path have no value.
     */
    public Object getDefaultValue() {
        return value;
    }
}
