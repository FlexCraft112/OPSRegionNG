package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, event.getBlock().getLocation(), true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, event.getBlock().getLocation(), false);
    }

    private void handle(Player player, Cancellable event, org.bukkit.Location loc, boolean breaking) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        Set<String> configRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        boolean allowedSomewhere = false;
        boolean checkedAny = false;

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!configRegions.contains(id)) continue;

            checkedAny = true;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) {
                allowedSomewhere = true;
                break;
            }
        }

        if (checkedAny && !allowedSomewhere) {
            String msgKey = breaking
                    ? "messages.break-blocked"
                    : "messages.place-blocked";

            String msg = plugin.getConfig()
                    .getString(msgKey, "&cДействие запрещено.")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
        }
    }
}
