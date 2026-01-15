package me.flexcraft.opsregionng;

import me.flexcraft.opsregionng.listener.WorldEditListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OPSRegionNG extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(
                new WorldEditListener(this),
                this
        );

        getLogger().info("OPSRegionNG enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("OPSRegionNG disabled");
    }
}
