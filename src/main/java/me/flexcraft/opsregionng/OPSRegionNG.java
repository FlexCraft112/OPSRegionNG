package me.flexcraft.opsregionng;

import org.bukkit.plugin.java.JavaPlugin;

public class OPSRegionNG extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("OPSRegionNG enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("OPSRegionNG disabled");
    }
}
