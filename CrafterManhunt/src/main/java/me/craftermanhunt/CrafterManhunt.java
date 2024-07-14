package me.craftermanhunt;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CrafterManhunt extends JavaPlugin {
    @Override
    public void onEnable() {
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        saveDefaultConfig();
        this.getCommand("crafter").setExecutor(new CrafterTimeCommand(this));
    }
}