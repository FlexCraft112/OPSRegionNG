package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ================= BREAK ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, "break");
    }

    /* ================= PLACE ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, "place");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent event) {
        handle(event.getPlayer(), event, "place");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        if (event.getPlayer() != null) {
            handle(event.getPlayer(), event, "place");
        }
    }

    /* ================= ITEMS ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;

        Material m = event.getItem().getType();

        if (
            m.name().contains("BOAT") ||
            m.name().contains("RAFT") ||
            m.name().contains("MINECART") ||
            m == Material.ARMOR_STAND ||
            m.name().contains("ITEM_FRAME") ||
            m.name().contains("SPAWN_EGG")
        ) {
            handle(event.getPlayer(), event, "place");
        }
    }

    /* ================= CORE ================= */

    private void handle(Player player, Cancellable event, String action) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        if (regions == null || regions.size() == 0) return;

        Set<String> cfgRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        boolean allowedSomewhere = false;
        boolean checked = false;

        for (ProtectedRegion region : regions) {
            String id = region.getId();
            if (!cfgRegions.contains(id)) continue;

            checked = true;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + "." + action, false
            );

            if (allowed) {
                allowedSomewhere = true;
                break;
            }
        }

        if (!checked || allowedSomewhere) return;

        String msg = plugin.getConfig()
                .getString("messages." + action + "-blocked", "&cЗапрещено.")
                .replace("&", "§");

        player.sendMessage(msg);
        event.setCancelled(true);
    }
}
