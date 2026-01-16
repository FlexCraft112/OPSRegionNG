package me.flexcraft.opsregionng.listener;

import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    // =========================
    // ЛОМАНИЕ БЛОКОВ
    // =========================
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        ApplicableRegionSet regions = getRegions(block);

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            // ✅ В АВТОШАХТЕ ЛОМАТЬ МОЖНО
            if (id.equalsIgnoreCase("mine")) return;

            // ❌ В ZONA ломать нельзя
            if (id.equalsIgnoreCase("zona")) {
                e.setCancelled(true);
                player.sendMessage(color(plugin.getConfig().getString("messages.break-blocked")));
                return;
            }

            if (!plugin.getConfig().getBoolean("regions." + id + ".break")) {
                e.setCancelled(true);
                player.sendMessage(color(plugin.getConfig().getString("messages.break-blocked")));
                return;
            }
        }
    }

    // =========================
    // УСТАНОВКА БЛОКОВ
    // =========================
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        denyPlace(e.getPlayer(), e.getBlock(), e);
    }

    // =========================
    // ЛОДКИ / ВЕДРА / СТОЙКИ / ВАГОНЕТКИ
    // =========================
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {

        if (!e.hasItem() || e.getClickedBlock() == null) return;

        ItemStack item = e.getItem();
        Material type = item.getType();
        String name = type.name();

        if (
                type == Material.ARMOR_STAND ||

                name.endsWith("_BOAT") ||
                name.endsWith("_CHEST_BOAT") ||
                name.endsWith("_RAFT") ||

                name.endsWith("_MINECART") ||

                type == Material.ITEM_FRAME ||
                type == Material.GLOW_ITEM_FRAME ||
                type == Material.PAINTING ||

                // 🪣 ВСЕ ВЁДРА (включая рыб)
                name.endsWith("_BUCKET")
        ) {
            denyPlace(e.getPlayer(), e.getClickedBlock(), e);
        }
    }

    // =========================
    // ОБЩИЙ ЗАПРЕТ УСТАНОВКИ
    // =========================
    private void denyPlace(Player player, Block block, Cancellable event) {

        if (player.hasPermission(plugin.getConfig().getString("bypass-permission")))
            return;

        ApplicableRegionSet regions = getRegions(block);

        for (ProtectedRegion region : regions) {

            String id = region.getId();

            // ❌ ZONA — ПОЛНЫЙ ЗАПРЕТ УСТАНОВКИ
            if (id.equalsIgnoreCase("zona")) {
                event.setCancelled(true);
                player.sendMessage(color(plugin.getConfig().getString("messages.place-blocked")));
                return;
            }

            // ❌ В MINE ставить нельзя
            if (id.equalsIgnoreCase("mine")) {
                event.setCancelled(true);
                player.sendMessage(color(plugin.getConfig().getString("messages.place-blocked")));
                return;
            }

            if (!plugin.getConfig().getBoolean("regions." + id + ".place")) {
                event.setCancelled(true);
                player.sendMessage(color(plugin.getConfig().getString("messages.place-blocked")));
                return;
            }
        }
    }

    // =========================
    // WORLDGUARD
    // =========================
    private ApplicableRegionSet getRegions(Block block) {
        return WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .createQuery()
                .getApplicableRegions(
                        BukkitAdapter.adapt(block.getLocation())
                );
    }

    private String color(String s) {
        return s == null ? "" : s.replace("&", "§");
    }
}
