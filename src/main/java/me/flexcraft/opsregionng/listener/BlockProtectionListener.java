package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

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

    /* ================= ARMOR STANDS / ENTITIES ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPlace(PlayerInteractEntityEvent e) {
        Entity ent = e.getRightClicked();

        if (ent instanceof ArmorStand ||
            ent instanceof Boat ||
            ent instanceof ChestBoat ||
            ent instanceof Minecart) {

            check(e.getPlayer(), ent.getLocation().getBlock(), e, false);
        }
    }

    /* ================= PAINTINGS / FRAMES ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent e) {
        check(e.getPlayer(), e.getBlock(), e, false);
    }

    /* ================= VEHICLES (RAFTS / BOATS) ================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent e) {

        if (!(e.getVehicle() instanceof Boat ||
              e.getVehicle() instanceof ChestBoat ||
              e.getVehicle() instanceof Minecart)) return;

        if (!(e.getVehicle().getWorld().getNearbyPlayers(
                e.getVehicle().getLocation(), 2).stream().findFirst().isPresent())) return;

        Player player = e.getVehicle().getWorld()
                .getNearbyPlayers(e.getVehicle().getLocation(), 2)
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
