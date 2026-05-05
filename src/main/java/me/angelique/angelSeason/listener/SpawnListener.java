package me.angelique.angelSeason.listener;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import me.angelique.angelSeason.config.PluginConfig;
import me.angelique.angelSeason.service.SeasonService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class SpawnListener implements Listener {

    private final SeasonService seasonService;
    private final PluginConfig config;

    public SpawnListener(SeasonService seasonService, PluginConfig config) {
        this.seasonService = seasonService;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreSpawn(PreCreatureSpawnEvent event) {
        World world = event.getSpawnLocation().getWorld();
        if (world == null || !config.isWorldEnabled(world)) {
            return;
        }
        if (event.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL && event.getReason() != CreatureSpawnEvent.SpawnReason.DEFAULT) {
            return;
        }
        if (!seasonService.isNaturalSpawnAllowed(world, event.getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        World world = event.getLocation().getWorld();
        if (world == null || !config.isWorldEnabled(world)) {
            return;
        }
        if (seasonService.isBloodMoonActive(world) && livingEntity instanceof Monster) {
            seasonService.applyBloodMoonBuffs(livingEntity);
            spawnExtraHostiles(world, event.getLocation(), event.getEntityType());
        }
    }

    private void spawnExtraHostiles(World world, Location location, EntityType sourceType) {
        if (world == null || location == null || sourceType == null) {
            return;
        }
        if (!sourceType.isSpawnable() || !sourceType.isAlive()) {
            return;
        }
        if (config.getExtraHostileSpawnAttempts() <= 0) {
            return;
        }

        for (int i = 0; i < config.getExtraHostileSpawnAttempts(); i++) {
            try {
                world.spawnEntity(location, sourceType, CreatureSpawnEvent.SpawnReason.CUSTOM);
            } catch (Exception ignored) {
                return;
            }
        }
    }
}
