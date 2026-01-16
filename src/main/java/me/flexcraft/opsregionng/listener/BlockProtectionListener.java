package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* =======================
       BLOCK BREAK / PLACE
       ======================= */

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event.getBlock(), event, true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event.getBlock(), event, false);
    }

    /* =======================
       BUCKETS (WATER / LAVA)
       ======================= */

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        handle(event.getPlayer(), event.getBlock(), event, false);
    }

    /* =======================
       ENTITY PLACE (boats, armorstand, etc)
       ======================= */

    @EventHandler
    public void onEntityPlace(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() != null)) return;
        Block block = event.getRightClicked().getLocation().getBlock();
        handle(event.getPlayer(), block, event, false);
    }

    /* =======================
       ITEM PLACE (boats, frames)
       ======================= */

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getClickedBlock() == null) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Material type = item.getType();

        // Всё что не блок — запрещаем
        if (!type.isBlock()) {
            handle(event.getPlayer(), event.getClickedBlock(), event, false);
        }
    }

    /* =======================
       FRAMES / PAINTINGS
       ======================= */

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        handle(event.getPlayer(), event.getBlock(), event, false);
    }

    /* =======================
       CORE LOGIC
       ======================= */

    private void handle(Player player, Block block, Cancellable event, boolean breaking) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(block.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));

        if (regions.isEmpty()) return;

        Set<String> configRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            if (!configRegions.contains(id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) return;

            String msgKey = breaking
                    ? "messages.break-blocked"
                    : "messages.place-blocked";

            String msg = plugin.getConfig()
                    .getString(msgKey, "&cДействие запрещено.")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
            return;
        }
    }
}
