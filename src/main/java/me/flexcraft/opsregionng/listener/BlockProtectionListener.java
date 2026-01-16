package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ===================== BREAK ===================== */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;

        if (isExplicitlyAllowed(player, "break")) {
            event.setCancelled(false);
            return;
        }

        deny(event, player, "messages.break-blocked");
    }

    /* ===================== PLACE ===================== */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;

        if (isExplicitlyAllowed(player, "place")) {
            event.setCancelled(false);
            return;
        }

        deny(event, player, "messages.place-blocked");
    }

    /* ===================== BUCKETS ===================== */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;

        if (isExplicitlyAllowed(player, "place")) {
            event.setCancelled(false);
            return;
        }

        deny(event, player, "messages.place-blocked");
    }

    /* ===================== ENTITIES (boats, armor, minecart) ===================== */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null || hasBypass(player)) return;

        if (isExplicitlyAllowed(player, "place")) {
            event.setCancelled(false);
            return;
        }

        deny(event, player, "messages.place-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null || hasBypass(player)) return;

        if (isExplicitlyAllowed(player, "place")) {
            event.setCancelled(false);
            return;
        }

        deny(event, player, "messages.place-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;

        if (isExplicitlyAllowed(player, "place")) {
            event.setCancelled(false);
            return;
        }

        deny(event, player, "messages.place-blocked");
    }

    /* ===================== CORE LOGIC ===================== */

    /**
     * ГЛАВНОЕ:
     * Если ХОТЯ БЫ ОДИН регион из конфига разрешает действие — РАЗРЕШАЕМ
     */
    private boolean isExplicitlyAllowed(Player player, String action) {
        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        boolean foundConfigRegion = false;

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!plugin.getConfig().contains("regions." + id)) continue;

            foundConfigRegion = true;

            boolean allowed = plugin.getConfig()
                    .getBoolean("regions." + id + "." + action, false);

            if (allowed) {
                return true; // 🔥 ХОТЯ БЫ ОДИН РАЗРЕШИЛ
            }
        }

        return !foundConfigRegion; // если регионов из конфига нет — не запрещаем
    }

    private boolean hasBypass(Player player) {
        String perm = plugin.getConfig().getString("bypass-permission");
        return perm != null && player.hasPermission(perm);
    }

    private void deny(Cancellable event, Player player, String key) {
        event.setCancelled(true);
        player.sendMessage(
                plugin.getConfig()
                        .getString(key, "&cЗапрещено.")
                        .replace("&", "§")
        );
    }
}
