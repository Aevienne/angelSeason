package me.angelique.angelSeason.storage;

import me.angelique.angelSeason.AngelSeason;
import me.angelique.angelSeason.model.SeasonType;
import me.angelique.angelSeason.model.WorldSeasonState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class WorldStateStorage {

    private final AngelSeason plugin;
    private final File file;

    public WorldStateStorage(AngelSeason plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public Map<String, WorldSeasonState> load() {
        Map<String, WorldSeasonState> result = new HashMap<>();
        if (!file.exists()) {
            return result;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("worlds");
        if (section == null) {
            return result;
        }
        for (String worldName : section.getKeys(false)) {
            WorldSeasonState state = new WorldSeasonState();
            String path = "worlds." + worldName;
            try {
                state.setCurrentSeason(SeasonType.valueOf(yaml.getString(path + ".season", "SPRING")));
            } catch (IllegalArgumentException ignored) {
                state.setCurrentSeason(SeasonType.SPRING);
            }
            state.setSeasonStartedAtMillis(yaml.getLong(path + ".season-started-at", System.currentTimeMillis()));
            state.setWorldDayCounter(yaml.getLong(path + ".world-day-counter", 0L));
            state.setBloodMoonActive(yaml.getBoolean(path + ".blood-moon-active", false));
            state.setLastCheckedMinecraftDay(yaml.getLong(path + ".last-checked-minecraft-day", -1L));
            result.put(worldName, state);
        }
        return result;
    }

    public void save(Map<String, WorldSeasonState> states) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, WorldSeasonState> entry : states.entrySet()) {
            String path = "worlds." + entry.getKey();
            WorldSeasonState state = entry.getValue();
            yaml.set(path + ".season", state.getCurrentSeason().name());
            yaml.set(path + ".season-started-at", state.getSeasonStartedAtMillis());
            yaml.set(path + ".world-day-counter", state.getWorldDayCounter());
            yaml.set(path + ".blood-moon-active", state.isBloodMoonActive());
            yaml.set(path + ".last-checked-minecraft-day", state.getLastCheckedMinecraftDay());
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder.");
                return;
            }
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save data.yml: " + exception.getMessage());
        }
    }
}
