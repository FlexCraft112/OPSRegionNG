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
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, false);
    }

    private void handle(Player player, Cancellable event, boolean breaking) {

        // bypass
        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        // RegionManager
        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(
                BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint()
        );

        if (!plugin.getConfig().isConfigurationSection("regions")) return;

        Set<String> regionKeys = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            if (!regionKeys.contains(id)) continue;

            // владельцы и участники — всегда можно
            if (region.isOwner(player.getUniqueId()) ||
                region.isMember(player.getUniqueId())) {
                return;
            }

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) return;

            String msgKey = breaking
                    ? "messages.break-blocked"
                    : "messages.place-blocked";

            String msg = plugin.getConfig()
                    .getString(msgKey, "&cДействие запрещено.")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
            return;
        }
    }
}
