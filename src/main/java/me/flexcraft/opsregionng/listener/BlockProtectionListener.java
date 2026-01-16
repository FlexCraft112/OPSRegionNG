package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Comparator;
import java.util.Set;

public class ExtraProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public ExtraProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ================= ВЁДРА ================= */

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        if (!check(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /* ================= СУЩНОСТИ (лодки, стойки, рамки и т.п.) ================= */

    @EventHandler
    public void onEntityPlace(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.hasItem()) return;

        switch (event.getMaterial()) {
            case ARMOR_STAND:
            case ITEM_FRAME:
            case GLOW_ITEM_FRAME:
            case PAINTING:
            case OAK_BOAT:
            case SPRUCE_BOAT:
            case BIRCH_BOAT:
            case JUNGLE_BOAT:
            case ACACIA_BOAT:
            case DARK_OAK_BOAT:
            case MANGROVE_BOAT:
            case BAMBOO_RAFT:
            case OAK_CHEST_BOAT:
            case SPRUCE_CHEST_BOAT:
            case BIRCH_CHEST_BOAT:
            case JUNGLE_CHEST_BOAT:
            case ACACIA_CHEST_BOAT:
            case DARK_OAK_CHEST_BOAT:
            case MANGROVE_CHEST_BOAT:
            case BAMBOO_CHEST_RAFT:
            case MINECART:
            case CHEST_MINECART:
            case HOPPER_MINECART:
            case TNT_MINECART:
            case FURNACE_MINECART:
                if (!check(event.getPlayer(), event.getPlayer().getLocation())) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }

    /* ================= КАРТИНЫ / РАМКИ ================= */

    @EventHandler
    public void onHanging(HangingPlaceEvent event) {
        if (event.getPlayer() == null) return;
        if (!check(event.getPlayer(), event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }

    /* ================= ОБЩАЯ ПРОВЕРКА ================= */

    private boolean check(Player player, Location loc) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return true;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        if (regions == null || regions.size() == 0) return true;

        Set<String> cfgRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        ProtectedRegion region = regions.getRegions().stream()
                .filter(r -> cfgRegions.contains(r.getId()))
                .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                .orElse(null);

        if (region == null) return true;

        return plugin.getConfig().getBoolean(
                "regions." + region.getId() + ".place",
                false
        );
    }
}
