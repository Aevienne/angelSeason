package me.angelique.angelSeason;

import me.angelique.angelSeason.command.SeasonCommand;
import me.angelique.angelSeason.config.PluginConfig;
import me.angelique.angelSeason.gui.SeasonGui;
import me.angelique.angelSeason.gui.SeasonListener;
import me.angelique.angelSeason.listener.BlockGrowthListener;
import me.angelique.angelSeason.listener.PlayerSleepListener;
import me.angelique.angelSeason.listener.SpawnListener;
import me.angelique.angelSeason.service.SeasonService;
import me.angelique.angelSeason.storage.WorldStateStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class AngelSeason extends JavaPlugin {

    private PluginConfig pluginConfig;
    private WorldStateStorage worldStateStorage;
    private SeasonService seasonService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("spawns.yml", false);
        reloadPlugin();
    }

    public void reloadPlugin() {
        HandlerList.unregisterAll(this);
        reloadConfig();

        this.pluginConfig = new PluginConfig(this);
        this.worldStateStorage = new WorldStateStorage(this);

        if (this.seasonService != null) {
            this.seasonService.shutdown();
        }

        this.seasonService = new SeasonService(this, pluginConfig, worldStateStorage);
        this.seasonService.initialize();

        Bukkit.getPluginManager().registerEvents(new BlockGrowthListener(seasonService, pluginConfig), this);
        Bukkit.getPluginManager().registerEvents(new SpawnListener(seasonService, pluginConfig), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSleepListener(seasonService, pluginConfig), this);
        Bukkit.getPluginManager().registerEvents(new SeasonListener(), this);

        PluginCommand command = getCommand("season");
        if (command != null) {
            SeasonCommand executor = new SeasonCommand(this, seasonService, pluginConfig);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        if (seasonService != null) {
            seasonService.shutdown();
        }
    }

    public SeasonService getSeasonService() { return seasonService; }
}
