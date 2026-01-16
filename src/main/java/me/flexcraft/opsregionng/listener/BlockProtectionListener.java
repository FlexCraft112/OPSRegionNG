package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;
    private final WorldGuardPlugin wg;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
        this.wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    /* ================= BREAK ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        check(e.getPlayer(), e.getBlock(), e, true);
    }

    /* ================= PLACE ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= BUCKETS ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= ARMOR STANDS / BOATS / FRAMES ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntity(PlayerInteractEntityEvent e) {
        check(e.getPlayer(), e.getRightClicked().getLocation().getBlock(), e, false);
    }

    /* ================= PAINTINGS ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= CORE ================= */

    private void check(Player player, Block block, Cancellable event, boolean breaking) {

        if (player.hasPermission(plugin.getConfig().getString("bypass-permission"))) return;

        RegionManager rm = wg.getRegionManager(block.getWorld());
        if (rm == null) return;

        ApplicableRegionSet regions =
                rm.getApplicableRegions(block.getLocation().toVector());

        if (regions.size() == 0) return;

        Set<String> cfgRegions =
                plugin.getConfig().getConfigurationSection("regions").getKeys(false);

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            if (!cfgRegions.contains(id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) return;

            player.sendMessage(plugin.getConfig()
                    .getString(breaking ? "messages.break-blocked" : "messages.place-blocked")
                    .replace("&", "§"));

            event.setCancelled(true);
            return;
        }
    }
}
