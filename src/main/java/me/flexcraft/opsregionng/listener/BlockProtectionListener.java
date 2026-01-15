package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, false);
    }

    private void handle(Player player, Cancellable event, boolean breaking) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(
                        BukkitAdapter.asBlockVector(player.getLocation())
                );

        boolean allow = false;
        boolean deny = false;

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            if (!plugin.getConfig().isConfigurationSection("regions." + id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) {
                allow = true;
            } else {
                deny = true;
            }
        }

        if (allow) return;

        if (deny) {
            String msg = plugin.getConfig()
                    .getString(breaking ? "messages.break-blocked" : "messages.place-blocked")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
        }
    }
}
