package me.angelique.angelSeason.listener;

import me.angelique.angelSeason.config.PluginConfig;
import me.angelique.angelSeason.service.SeasonService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public final class PlayerSleepListener implements Listener {

    private final SeasonService seasonService;
    private final PluginConfig config;

    public PlayerSleepListener(SeasonService seasonService, PluginConfig config) {
        this.seasonService = seasonService;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!config.isNoSleep()) {
            return;
        }
        if (!config.isWorldEnabled(event.getPlayer().getWorld())) {
            return;
        }
        if (!seasonService.isBloodMoonActive(event.getPlayer().getWorld())) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(config.getPrefix() + "You cannot sleep during a Blood Moon.");
    }
}
