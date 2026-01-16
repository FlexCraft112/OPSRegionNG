package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* =========================
       BLOCK BREAK / PLACE
       ========================= */

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!check(e.getPlayer(), e.getBlock().getLocation(), "break")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!check(e.getPlayer(), e.getBlock().getLocation(), "place")) {
            e.setCancelled(true);
        }
    }

    /* =========================
       BUCKETS (WATER / LAVA)
       ========================= */

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent e) {
        if (!check(e.getPlayer(), e.getBlockClicked().getLocation(), "place")) {
            e.setCancelled(true);
        }
    }

    /* =========================
       ARMOR STANDS / BOATS / ETC
       ========================= */

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;

        if (!check(p, e.getEntity().getLocation(), "place")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent e) {
        if (!check(e.getPlayer(), e.getEntity().getLocation(), "place")) {
            e.setCancelled(true);
        }
    }

    /* =========================
       CORE REGION CHECK
       ========================= */

    private boolean check(Player player, Location loc, String action) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return true;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(WorldGuardPlugin.inst().getAdapter().adapt(loc.getWorld()))
                .getApplicableRegions(WorldGuardPlugin.inst().getAdapter().asBlockVector(loc));

        for (ProtectedRegion region : regions) {

            String id = region.getId();

            if (!plugin.getConfig().contains("regions." + id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + "." + action,
                    false
            );

            if (!allowed) {
                player.sendMessage(
                        plugin.getConfig()
                                .getString("messages." + action + "-blocked", "&cЗапрещено.")
                                .replace("&", "§")
                );
                return false;
            }
        }
        return true;
    }
}
