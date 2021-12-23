package com.georgev22.hunter.utilities.configmanager;

import com.georgev22.api.configmanager.CFG;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FileManager {

    private static FileManager instance;

    public static FileManager getInstance() {
        return instance == null ? instance = new FileManager() : instance;
    }

    private CFG config, messages, discord, data, prestige, items;


    private FileManager() {
    }

    public void loadFiles(final JavaPlugin plugin) {
        this.messages = new CFG(plugin, "messages", false);
        this.config = new CFG(plugin, "config", true);
        this.discord = new CFG(plugin, "discord", true);
        this.data = new CFG(plugin, "data", false);
        this.prestige = new CFG(plugin, "inventories" + File.separator + "prestige" + File.separator + "prestige_0", true);
        this.items = new CFG(plugin, "items", true);
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

    public CFG getPrestige() {
        return prestige;
    }

    public CFG getItems() {
        return items;
    }
}
