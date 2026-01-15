package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class WorldEditListener implements Listener {

    private final OPSRegionNG plugin;

    public WorldEditListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldEdit(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();

        if (!msg.startsWith("//") && !msg.startsWith("/we")) return;

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(
                        BukkitAdapter.asBlockVector(player.getLocation())
                );

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!plugin.getConfig().isConfigurationSection("regions." + id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + ".worldedit",
                    false
            );

            if (!allowed) {
                player.sendMessage(
                        plugin.getConfig()
                                .getString("messages.worldedit-blocked")
                                .replace("&", "§")
                );
                event.setCancelled(true);
                return;
            }
        }
    }
}
