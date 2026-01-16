package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;
    private final RegionQuery query;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        this.query = container.createQuery();
    }

    /* ================= BLOCK BREAK ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        check(e.getPlayer(), e.getBlock(), e, true);
    }

    /* ================= BLOCK PLACE ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= BUCKETS ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= ARMOR STAND (ITEM) ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {

        if (!e.hasItem() || e.getClickedBlock() == null) return;

        ItemStack item = e.getItem();
        Material type = item.getType();

        if (type == Material.ARMOR_STAND ||
            type == Material.BAMBOO_RAFT ||
            type == Material.BAMBOO_CHEST_RAFT ||
            type == Material.MINECART ||
            type == Material.CHEST_MINECART ||
            type == Material.FURNACE_MINECART ||
            type == Material.HOPPER_MINECART ||
            type == Material.TNT_MINECART) {

            check(e.getPlayer(), e.getClickedBlock(), e, false);
        }
    }

    /* ================= PAINTINGS / FRAMES ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= BOATS / RAFTS / MINECARTS ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent e) {

        EntityType type = e.getVehicle().getType();

        if (type != EntityType.BOAT &&
            type != EntityType.CHEST_BOAT &&
            type != EntityType.MINECART &&
            type != EntityType.BAMBOO_RAFT &&
            type != EntityType.BAMBOO_CHEST_RAFT) return;

        Player player = e.getVehicle().getWorld()
                .getNearbyPlayers(e.getVehicle().getLocation(), 3)
                .stream().findFirst().orElse(null);

        if (player == null) return;

        check(player, e.getVehicle().getLocation().getBlock(), e, false);
    }

    /* ================= CORE ================= */

    private void check(Player player, Block block, Cancellable event, boolean breaking) {

        if (player.hasPermission(plugin.getConfig().getString("bypass-permission"))) return;

        ApplicableRegionSet regions =
                query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

        if (!regions.iterator().hasNext()) return;

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

            player.sendMessage(
                    plugin.getConfig()
                            .getString(breaking ? "messages.break-blocked" : "messages.place-blocked")
                            .replace("&", "§")
            );

            event.setCancelled(true);
            return;
        }
    }
}
