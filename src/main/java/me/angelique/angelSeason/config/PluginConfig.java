package me.angelique.angelSeason.config;

import me.angelique.angelSeason.AngelSeason;
import me.angelique.angelSeason.model.SeasonType;
import me.angelique.angelSeason.model.SpawnRules;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PluginConfig {

    private final AngelSeason plugin;
    private final long tickInterval;
    private final long saveIntervalTicks;
    private final boolean sidebarEnabled;
    private final boolean bossbarEnabled;
    private final boolean actionbarEnabled;
    private final Set<String> enabledWorlds;
    private final boolean useAllWorldsWhenEmpty;
    private final Map<SeasonType, Long> seasonLengthSeconds = new EnumMap<>(SeasonType.class);
    private final Map<SeasonType, Double> cropGrowthMultipliers = new EnumMap<>(SeasonType.class);
    private final boolean bloodMoonEnabled;
    private final int eclipseIntervalDays;
    private final boolean requireFullMoon;
    private final String bloodMoonSidebarLabel;
    private final String bloodMoonBroadcastStart;
    private final String bloodMoonBroadcastEnd;
    private final boolean noSleep;
    private final int extraHostileSpawnAttempts;
    private final double healthMultiplier;
    private final double damageMultiplier;
    private final double speedMultiplier;
    private final boolean glowingMobs;
    private final boolean customMobsEnabled;
    private final String customMobProvider;
    private final String prefix;
    private final String seasonStatus;
    private final String seasonSet;
    private final String seasonNext;
    private final String reload;
    private final Map<SeasonType, SpawnRules> spawnRules = new HashMap<>();

    public PluginConfig(AngelSeason plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.tickInterval = Math.max(20L, config.getLong("general.tick-interval", 200L));
        this.saveIntervalTicks = Math.max(20L, config.getLong("general.save-interval-seconds", 60L) * 20L);
        this.sidebarEnabled = config.getBoolean("general.sidebar-enabled", true);
        this.bossbarEnabled = config.getBoolean("general.bossbar-enabled", true);
        this.actionbarEnabled = config.getBoolean("general.actionbar-enabled", false);
        this.enabledWorlds = new HashSet<>(config.getStringList("general.enabled-worlds"));
        this.useAllWorldsWhenEmpty = config.getBoolean("general.use-all-worlds-when-empty", true);

        for (SeasonType seasonType : SeasonType.values()) {
            seasonLengthSeconds.put(seasonType, Math.max(60L, config.getLong("season-lengths-seconds." + seasonType.name(), 86400L)));
            cropGrowthMultipliers.put(seasonType, Math.max(0.0D, config.getDouble("crop-growth-multipliers." + seasonType.name(), 1.0D)));
        }

        this.bloodMoonEnabled = config.getBoolean("blood-moon.enabled", true);
        this.eclipseIntervalDays = Math.max(1, config.getInt("blood-moon.eclipse-interval-days", 8));
        this.requireFullMoon = config.getBoolean("blood-moon.require-full-moon", true);
        this.bloodMoonSidebarLabel = color(config.getString("blood-moon.sidebar-label", "&4Blood Moon"));
        this.bloodMoonBroadcastStart = color(config.getString("blood-moon.broadcast-start", "&4A Blood Moon rises over {world}!"));
        this.bloodMoonBroadcastEnd = color(config.getString("blood-moon.broadcast-end", "&cThe Blood Moon has ended in {world}."));
        this.noSleep = config.getBoolean("blood-moon.no-sleep", true);
        this.extraHostileSpawnAttempts = Math.max(0, config.getInt("blood-moon.extra-hostile-spawn-attempts", 2));
        this.healthMultiplier = Math.max(1.0D, config.getDouble("blood-moon.health-multiplier", 1.5D));
        this.damageMultiplier = Math.max(1.0D, config.getDouble("blood-moon.damage-multiplier", 1.35D));
        this.speedMultiplier = Math.max(1.0D, config.getDouble("blood-moon.speed-multiplier", 1.15D));
        this.glowingMobs = config.getBoolean("blood-moon.glowing-mobs", false);
        this.customMobsEnabled = config.getBoolean("custom-mobs.enabled", true);
        this.customMobProvider = config.getString("custom-mobs.provider", "INTERNAL");
        this.prefix = color(config.getString("messages.prefix", "&6[AngelSeason] &r"));
        this.seasonStatus = color(config.getString("messages.season-status", "&eWorld: &f{world} &7| &eSeason: &f{season} &7| &eBlood Moon: &f{bloodmoon}"));
        this.seasonSet = color(config.getString("messages.season-set", "&aSet season for {world} to {season}."));
        this.seasonNext = color(config.getString("messages.season-next", "&aAdvanced season for {world} to {season}."));
        this.reload = color(config.getString("messages.reload", "&aAngelSeason reloaded."));
        loadSpawnRules();
    }

    private void loadSpawnRules() {
        File file = new File(plugin.getDataFolder(), "spawns.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Set<EntityType> bloodMoonExtraAllowed = parseEntityTypes(yaml.getStringList("blood-moon.natural.extra-allowed"));
        Set<String> bloodMoonExtraCustomIds = new HashSet<>(yaml.getStringList("blood-moon.custom.extra-allowed"));

        for (SeasonType seasonType : SeasonType.values()) {
            String path = "spawns." + seasonType.name();
            Set<EntityType> allowedNatural = parseEntityTypes(yaml.getStringList(path + ".natural.allowed"));
            Set<EntityType> blockedNatural = parseEntityTypes(yaml.getStringList(path + ".natural.blocked"));
            Set<String> allowedCustomMobIds = new HashSet<>(yaml.getStringList(path + ".custom.allowed"));
            spawnRules.put(seasonType, new SpawnRules(allowedNatural, blockedNatural, allowedCustomMobIds, bloodMoonExtraAllowed, bloodMoonExtraCustomIds));
        }
    }

    private Set<EntityType> parseEntityTypes(List<String> names) {
        Set<EntityType> entityTypes = new HashSet<>();
        for (String name : names) {
            try {
                entityTypes.add(EntityType.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return entityTypes;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    public boolean isWorldEnabled(World world) {
        if (useAllWorldsWhenEmpty && enabledWorlds.isEmpty()) {
            return true;
        }
        return enabledWorlds.contains(world.getName());
    }

    public long getTickInterval() { return tickInterval; }
    public long getSaveIntervalTicks() { return saveIntervalTicks; }
    public boolean isSidebarEnabled() { return sidebarEnabled; }
    public boolean isBossbarEnabled() { return bossbarEnabled; }
    public boolean isActionbarEnabled() { return actionbarEnabled; }
    public long getSeasonLengthSeconds(SeasonType seasonType) { return seasonLengthSeconds.getOrDefault(seasonType, 86400L); }
    public double getCropGrowthMultiplier(SeasonType seasonType) { return cropGrowthMultipliers.getOrDefault(seasonType, 1.0D); }
    public boolean isBloodMoonEnabled() { return bloodMoonEnabled; }
    public int getEclipseIntervalDays() { return eclipseIntervalDays; }
    public boolean isRequireFullMoon() { return requireFullMoon; }
    public String getBloodMoonSidebarLabel() { return bloodMoonSidebarLabel; }
    public String getBloodMoonBroadcastStart() { return bloodMoonBroadcastStart; }
    public String getBloodMoonBroadcastEnd() { return bloodMoonBroadcastEnd; }
    public boolean isNoSleep() { return noSleep; }
    public int getExtraHostileSpawnAttempts() { return extraHostileSpawnAttempts; }
    public double getHealthMultiplier() { return healthMultiplier; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public boolean isGlowingMobs() { return glowingMobs; }
    public boolean isCustomMobsEnabled() { return customMobsEnabled; }
    public String getCustomMobProvider() { return customMobProvider; }
    public String getPrefix() { return prefix; }
    public String getSeasonStatus() { return seasonStatus; }
    public String getSeasonSet() { return seasonSet; }
    public String getSeasonNext() { return seasonNext; }
    public String getReload() { return reload; }
    public SpawnRules getSpawnRules(SeasonType seasonType) { return spawnRules.get(seasonType); }
}
