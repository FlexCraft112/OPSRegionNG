package me.flexcraft.opsregionng;

import me.flexcraft.opsregionng.listener.WorldEditListener;
import me.flexcraft.opsregionng.listener.BlockProtectionListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OPSRegionNG extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // WorldEdit запреты
        Bukkit.getPluginManager().registerEvents(
                new WorldEditListener(this),
                this
        );

        // Защита ломания / строительства
        Bukkit.getPluginManager().registerEvents(
                new BlockProtectionListener(this),
                this
        );

        getLogger().info("OPSRegionNG enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("OPSRegionNG disabled");
    }
}
