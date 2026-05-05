package me.angelique.angelSeason.listener;

import me.angelique.angelSeason.config.PluginConfig;
import me.angelique.angelSeason.service.SeasonService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockGrowthListener implements Listener {

    private static final Set<Material> CROPS = EnumSet.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.COCOA,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM,
            Material.SWEET_BERRY_BUSH,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP
    );

    private final SeasonService seasonService;
    private final PluginConfig config;

    public BlockGrowthListener(SeasonService seasonService, PluginConfig config) {
        this.seasonService = seasonService;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (!config.isWorldEnabled(block.getWorld())) {
            return;
        }
        if (!CROPS.contains(block.getType())) {
            return;
        }
        double multiplier = config.getCropGrowthMultiplier(seasonService.getSeason(block.getWorld()));
        if (multiplier <= 0.0D) {
            event.setCancelled(true);
            return;
        }
        if (multiplier < 1.0D) {
            double allowedChance = Math.max(0.0D, Math.min(1.0D, multiplier));
            if (ThreadLocalRandom.current().nextDouble() > allowedChance) {
                event.setCancelled(true);
            }
        }
    }
}
