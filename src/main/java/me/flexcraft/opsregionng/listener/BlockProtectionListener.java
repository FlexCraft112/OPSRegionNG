package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (hasBypass(player)) return;

        if (!isAllowed(player, "break")) {
            deny(event, player, "messages.break-blocked");
        }
    }

    /* ===================== PLACE ===================== */

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (hasBypass(player)) return;

        if (!isAllowed(player, "place")) {
            deny(event, player, "messages.place-blocked");
        }
    }

    /* ===================== BUCKETS ===================== */

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        if (hasBypass(player)) return;

        if (!isAllowed(player, "place")) {
            deny(event, player, "messages.place-blocked");
        }
    }

    /* ===================== BOATS / MINECARTS / ARMOR ===================== */

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (hasBypass(player)) return;

        if (!isAllowed(player, "place")) {
            deny(event, player, "messages.place-blocked");
        }
    }

    @EventHandler
    public void onHanging(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        if (hasBypass(player)) return;

        if (!isAllowed(player, "place")) {
            deny(event, player, "messages.place-blocked");
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (hasBypass(player)) return;

        if (!isAllowed(player, "place")) {
            deny(event, player, "messages.place-blocked");
        }
    }

    /* ===================== CORE ===================== */

    private boolean isAllowed(Player player, String action) {
        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!plugin.getConfig().contains("regions." + id)) continue;

            boolean allowed = plugin.getConfig()
                    .getBoolean("regions." + id + "." + action, false);

            if (!allowed) return false;
        }
        return true;
    }

    private boolean hasBypass(Player player) {
        String perm = plugin.getConfig().getString("bypass-permission");
        return perm != null && player.hasPermission(perm);
    }

    private void deny(org.bukkit.event.Cancellable event, Player player, String msgKey) {
        event.setCancelled(true);
        player.sendMessage(plugin.getConfig()
                .getString(msgKey, "&cЗапрещено.")
                .replace("&", "§"));
    }
}
