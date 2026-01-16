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
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Comparator;
import java.util.Optional;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ================= BREAK ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (hasBypass(e.getPlayer())) return;
        handle(e, e.getPlayer(), "break", "messages.break-blocked");
    }

    /* ================= PLACE ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (hasBypass(e.getPlayer())) return;
        handle(e, e.getPlayer(), "place", "messages.place-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent e) {
        if (hasBypass(e.getPlayer())) return;
        handle(e, e.getPlayer(), "place", "messages.place-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntity(EntityPlaceEvent e) {
        if (e.getPlayer() == null || hasBypass(e.getPlayer())) return;
        handle(e, e.getPlayer(), "place", "messages.place-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHanging(HangingPlaceEvent e) {
        if (e.getPlayer() == null || hasBypass(e.getPlayer())) return;
        handle(e, e.getPlayer(), "place", "messages.place-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent e) {
        if (hasBypass(e.getPlayer())) return;
        handle(e, e.getPlayer(), "place", "messages.place-blocked");
    }

    /* ================= CORE ================= */

    private void handle(Cancellable event, Player player, String action, String messageKey) {

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        Optional<ProtectedRegion> topRegion = regions.getRegions().stream()
                .filter(r -> plugin.getConfig().contains("regions." + r.getId()))
                .max(Comparator.comparingInt(ProtectedRegion::getPriority));

        if (topRegion.isEmpty()) return; // регионов из конфига нет → разрешаем

        ProtectedRegion region = topRegion.get();
        boolean allowed = plugin.getConfig()
                .getBoolean("regions." + region.getId() + "." + action, false);

        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(
                    plugin.getConfig()
                            .getString(messageKey, "&cЗапрещено.")
                            .replace("&", "§")
            );
        }
    }

    private boolean hasBypass(Player p) {
        String perm = plugin.getConfig().getString("bypass-permission");
        return perm != null && p.hasPermission(perm);
    }
}
