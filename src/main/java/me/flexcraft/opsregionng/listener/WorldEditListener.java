package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

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

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        String msg = event.getMessage().toLowerCase();
        if (!msg.startsWith("//") && !msg.startsWith("/we")) return;

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(
                BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint()
        );

        for (ProtectedRegion region : regions) {
            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + region.getId() + ".worldedit", true
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
