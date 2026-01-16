package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;
    private final WorldGuardPlugin worldGuard;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    /* ================= BLOCKS ================= */

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        handle(e.getPlayer(), e, true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        handle(e.getPlayer(), e, false);
    }

    /* ================= BUCKETS ================= */

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent e) {
        handle(e.getPlayer(), e, false);
    }

    /* ================= FRAMES / PAINTINGS ================= */

    @EventHandler
    public void onHanging(HangingPlaceEvent e) {
        handle(e.getPlayer(), e, false);
    }

    /* ================= BOATS / RAFTS / MINECARTS ================= */

    @EventHandler
    public void onVehicle(VehicleCreateEvent e) {
        if (!(e.getVehicle().getPassenger() instanceof Player player)) return;
        handle(player, e, false);
    }

    /* ================= INTERACTIONS (ARMOR STANDS ETC) ================= */

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;
        handle(e.getPlayer(), e, false);
    }

    /* ================= CORE ================= */

    private void handle(Player player, Cancellable event, boolean breaking) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        if (worldGuard == null) return;

        RegionManager manager = worldGuard.getRegionContainer().get(player.getWorld());
        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(player.getLocation().toVector().toBlockPoint());

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            String base = "regions." + id;

            if (!plugin.getConfig().contains(base)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    base + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) return;

            String msgKey = breaking ? "messages.break-blocked" : "messages.place-blocked";
            String msg = plugin.getConfig().getString(msgKey, "&cДействие запрещено.")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
            return;
        }
    }
}
