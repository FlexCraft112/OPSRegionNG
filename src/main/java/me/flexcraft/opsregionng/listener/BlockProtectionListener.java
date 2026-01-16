package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!check(e.getPlayer(), "break")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(color(
                plugin.getConfig().getString("messages.break-blocked")
            ));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!check(e.getPlayer(), "place")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(color(
                plugin.getConfig().getString("messages.place-blocked")
            ));
        }
    }

    private boolean check(Player player, String action) {

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
                    "regions." + id + "." + action,
                    false
            );
        }
        return true; // вне регионов — разрешено
    }

    private String color(String s) {
        return s == null ? "" : s.replace("&", "§");
    }
}
