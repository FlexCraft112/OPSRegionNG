package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class ExtraProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public ExtraProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;

        Material m = e.getItem().getType();

        if (m == Material.WATER_BUCKET || m == Material.LAVA_BUCKET) {
            if (!allowed(e.getPlayer())) e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent e) {
        if (!allowed(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicle(VehicleCreateEvent e) {
        if (!(e.getVehicle().getPassenger() instanceof Player p)) return;
        if (!allowed(p)) e.setCancelled(true);
    }

    private boolean allowed(Player player) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return true;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!plugin.getConfig().contains("regions." + id)) continue;

            return plugin.getConfig().getBoolean(
                    "regions." + id + ".place",
                    false
            );
        }
        return true;
    }
}
