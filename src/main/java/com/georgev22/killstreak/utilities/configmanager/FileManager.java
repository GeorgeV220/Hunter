package com.georgev22.killstreak.utilities.configmanager;

import com.georgev22.api.configmanager.CFG;
import org.bukkit.plugin.java.JavaPlugin;

public final class FileManager {

    private static FileManager instance;

    public static FileManager getInstance() {
        return instance == null ? instance = new FileManager() : instance;
    }

    private CFG config, messages, discord, data;


    private FileManager() {
    }

    public void loadFiles(final JavaPlugin plugin) {
        this.messages = new CFG(plugin, "messages", false);
        this.config = new CFG(plugin, "config", true);
        this.discord = new CFG(plugin, "discord", true);
        this.data = new CFG(plugin, "data", false);
    }

    public CFG getMessages() {
        return messages;
    }

    public CFG getConfig() {
        return config;
    }

    public CFG getDiscord() {
        return discord;
    }

    public CFG getData() {
        return data;
    }
}
