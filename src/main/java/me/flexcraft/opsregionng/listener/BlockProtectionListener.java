package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ================= ЛОМАНИЕ ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, "break");
    }

    /* ================= СТРОИТЕЛЬСТВО ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, "place");
    }

    /* ================= ВЁДРА ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent event) {
        handle(event.getPlayer(), event, "place");
    }

    /* ================= СТОЙКИ / СУЩНОСТИ ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        handle(event.getPlayer(), event, "place");
    }

    /* ================= РАМКИ / КАРТИНЫ ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent event) {
        if (event.getPlayer() != null) {
            handle(event.getPlayer(), event, "place");
        }
    }

    /* ================= ЛОДКИ / ВАГОНЕТКИ ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (event.getVehicle().getLocation().getWorld() == null) return;

        Player player = event.getVehicle().getPassengers()
                .stream()
                .filter(p -> p instanceof Player)
                .map(p -> (Player) p)
                .findFirst()
                .orElse(null);

        if (player != null) {
            handle(player, event, "place");
        }
    }

    /* ================= ОБЩАЯ ЛОГИКА ================= */

    private void handle(Player player, Cancellable event, String action) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        if (regions == null) return;

        Set<String> cfgRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            if (!cfgRegions.contains(id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + "." + action, false
            );

            if (allowed) return;

            String msg = plugin.getConfig()
                    .getString("messages." + action + "-blocked", "&cЗапрещено.")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
            return;
        }
    }
}
