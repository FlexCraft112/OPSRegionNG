package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, false);
    }

    private void handle(Player player, Cancellable event, boolean breaking) {

        // bypass
        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) {
            return;
        }

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(
                        BukkitAdapter.asBlockVector(player.getLocation())
                );

        boolean regionFound = false;

        for (ProtectedRegion region : regions) {

            String id = region.getId();

            // если регион не описан в конфиге — игнор
            if (!plugin.getConfig().isConfigurationSection("regions." + id)) {
                continue;
            }

            regionFound = true;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            // 🔥 ЕСЛИ ХОТЯ БЫ ОДИН РЕГИОН РАЗРЕШАЕТ — РАЗРЕШАЕМ
            if (allowed) {
                return;
            }
        }

        // ❌ если регион найден, но ни один не разрешил
        if (regionFound) {
            String msg = plugin.getConfig()
                    .getString(
                            breaking ? "messages.break-blocked" : "messages.place-blocked",
                            "&cДействие запрещено."
                    )
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
        }
    }
}
