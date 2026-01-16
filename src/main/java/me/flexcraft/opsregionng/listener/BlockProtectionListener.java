package me.flexcraft.opsregionng.listener;

import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
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
        check(e.getPlayer(), e.getBlock(), e);
    }

    // =========================
    // УСТАНОВКА БЛОКОВ
    // =========================
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        check(e.getPlayer(), e.getBlock(), e);
    }

    // =========================
    // ВСЁ НЕ-БЛОЧНОЕ (лодки, ведра, стойки и т.д.)
    // =========================
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {

        if (!e.hasItem() || e.getClickedBlock() == null) return;

        ItemStack item = e.getItem();
        Material type = item.getType();
        String name = type.name();

        if (
                // стойки
                type == Material.ARMOR_STAND ||

                // лодки / плоты / chest версии
                name.endsWith("_BOAT") ||
                name.endsWith("_CHEST_BOAT") ||
                name.endsWith("_RAFT") ||

                // вагонетки
                name.endsWith("_MINECART") ||

                // рамки и картины
                type == Material.ITEM_FRAME ||
                type == Material.GLOW_ITEM_FRAME ||
                type == Material.PAINTING ||

                // ВСЕ ведра (вода, лава, рыбы, аксолотли и т.д.)
                (name.endsWith("_BUCKET") && type != Material.MILK_BUCKET)
        ) {
            check(e.getPlayer(), e.getClickedBlock(), e);
        }
    }

    // =========================
    // ОБЩАЯ ПРОВЕРКА РЕГИОНА
    // =========================
    private void check(Player player, Block block, org.bukkit.event.Event event) {

        if (player.hasPermission(plugin.getConfig().getString("bypass-permission")))
            return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .createQuery()
                .getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

        for (ProtectedRegion region : regions) {

            String id = region.getId();

            if (!plugin.getConfig().contains("regions." + id))
                continue;

            boolean allowed;

            if (event instanceof BlockBreakEvent) {
                allowed = plugin.getConfig().getBoolean("regions." + id + ".break");
                if (!allowed) {
                    ((BlockBreakEvent) event).setCancelled(true);
                    player.sendMessage(color(plugin.getConfig().getString("messages.break-blocked")));
                    return;
                }
            }

            if (event instanceof BlockPlaceEvent || event instanceof PlayerInteractEvent) {
                allowed = plugin.getConfig().getBoolean("regions." + id + ".place");
                if (!allowed) {
                    event.setCancelled(true);
                    player.sendMessage(color(plugin.getConfig().getString("messages.place-blocked")));
                    return;
                }
            }
        }
    }

    private String color(String s) {
        return s == null ? "" : s.replace("&", "§");
    }
}
