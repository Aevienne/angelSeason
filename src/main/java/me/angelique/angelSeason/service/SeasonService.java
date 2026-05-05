package me.angelique.angelSeason.service;

import me.angelique.angelSeason.AngelSeason;
import me.angelique.angelSeason.config.PluginConfig;
import me.angelique.angelSeason.model.SeasonType;
import me.angelique.angelSeason.model.SpawnRules;
import me.angelique.angelSeason.model.WorldSeasonState;
import me.angelique.angelSeason.storage.WorldStateStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public final class SeasonService {

    private static final String SIDEBAR_OBJECTIVE = "aseason";

    private final AngelSeason plugin;
    private final PluginConfig config;
    private final WorldStateStorage storage;
    private final Map<String, WorldSeasonState> worldStates = new HashMap<>();
    private final Map<String, BossBar> bloodMoonBars = new HashMap<>();
    private int tickTaskId = -1;
    private int saveTaskId = -1;
    private boolean dirty;

    public SeasonService(AngelSeason plugin, PluginConfig config, WorldStateStorage storage) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
    }

    public void initialize() {
        worldStates.clear();
        worldStates.putAll(storage.load());
        for (World world : Bukkit.getWorlds()) {
            if (config.isWorldEnabled(world)) {
                worldStates.computeIfAbsent(world.getName(), key -> new WorldSeasonState());
            }
        }
        startTasks();
        refreshDisplays();
    }

    public void shutdown() {
        if (tickTaskId != -1) {
            Bukkit.getScheduler().cancelTask(tickTaskId);
            tickTaskId = -1;
        }
        if (saveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(saveTaskId);
            saveTaskId = -1;
        }
        for (BossBar bossBar : bloodMoonBars.values()) {
            bossBar.removeAll();
        }
        bloodMoonBars.clear();
        flush();
    }

    public SeasonType getSeason(World world) {
        return getState(world).getCurrentSeason();
    }

    public boolean isBloodMoonActive(World world) {
        return getState(world).isBloodMoonActive();
    }

    public SpawnRules getSpawnRules(World world) {
        return config.getSpawnRules(getSeason(world));
    }

    public void setSeason(World world, SeasonType seasonType) {
        WorldSeasonState state = getState(world);
        state.setCurrentSeason(seasonType);
        state.setSeasonStartedAtMillis(System.currentTimeMillis());
        markDirty();
        refreshDisplays();
    }

    public SeasonType advanceSeason(World world) {
        WorldSeasonState state = getState(world);
        SeasonType next = state.getCurrentSeason().next();
        setSeason(world, next);
        return next;
    }

    public void setBloodMoon(World world, boolean active) {
        WorldSeasonState state = getState(world);
        if (state.isBloodMoonActive() == active) {
            return;
        }
        state.setBloodMoonActive(active);
        markDirty();
        if (active) {
            Bukkit.broadcastMessage(config.getBloodMoonBroadcastStart().replace("{world}", world.getName()));
        } else {
            Bukkit.broadcastMessage(config.getBloodMoonBroadcastEnd().replace("{world}", world.getName()));
        }
        refreshDisplays();
    }

    public void refreshDisplays() {
        updateBossBars();
        updateSidebars();
    }

    public void applyBloodMoonBuffs(LivingEntity entity) {
        if (!(entity instanceof Monster monster)) {
            return;
        }
        multiplyAttribute(monster, Attribute.MAX_HEALTH, config.getHealthMultiplier(), true);
        multiplyAttribute(monster, Attribute.ATTACK_DAMAGE, config.getDamageMultiplier(), false);
        multiplyAttribute(monster, Attribute.MOVEMENT_SPEED, config.getSpeedMultiplier(), false);
        if (config.isGlowingMobs()) {
            monster.setGlowing(true);
        }
    }

    public boolean isCustomMobAllowed(World world, String customId) {
        if (!config.isCustomMobsEnabled() || customId == null || customId.isBlank()) {
            return false;
        }
        SpawnRules rules = getSpawnRules(world);
        if (rules == null) {
            return false;
        }
        if (isBloodMoonActive(world) && rules.getBloodMoonExtraCustomIds().contains(customId)) {
            return true;
        }
        return rules.getAllowedCustomMobIds().contains(customId);
    }

    public boolean isNaturalSpawnAllowed(World world, EntityType entityType) {
        SpawnRules rules = getSpawnRules(world);
        if (rules == null) {
            return true;
        }
        if (rules.getBlockedNatural().contains(entityType)) {
            return false;
        }
        if (!rules.getAllowedNatural().isEmpty() && !rules.getAllowedNatural().contains(entityType)) {
            if (!(isBloodMoonActive(world) && rules.getBloodMoonExtraAllowed().contains(entityType))) {
                return false;
            }
        }
        return true;
    }

    private void startTasks() {
        tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, config.getTickInterval(), config.getTickInterval());
        saveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::flush, config.getSaveIntervalTicks(), config.getSaveIntervalTicks());
    }

    private void tick() {
        long now = System.currentTimeMillis();
        boolean changed = false;

        for (World world : Bukkit.getWorlds()) {
            if (!config.isWorldEnabled(world)) {
                continue;
            }

            WorldSeasonState state = getState(world);
            long lengthMillis = config.getSeasonLengthSeconds(state.getCurrentSeason()) * 1000L;
            if (now - state.getSeasonStartedAtMillis() >= lengthMillis) {
                state.setCurrentSeason(state.getCurrentSeason().next());
                state.setSeasonStartedAtMillis(now);
                changed = true;
            }

            long minecraftDay = world.getFullTime() / 24000L;
            if (minecraftDay != state.getLastCheckedMinecraftDay()) {
                if (minecraftDay > state.getLastCheckedMinecraftDay()) {
                    state.setWorldDayCounter(state.getWorldDayCounter() + 1L);
                }
                state.setLastCheckedMinecraftDay(minecraftDay);
                changed = true;
            }

            boolean shouldBloodMoon = shouldActivateBloodMoon(world, state);
            if (shouldBloodMoon != state.isBloodMoonActive()) {
                state.setBloodMoonActive(shouldBloodMoon);
                if (shouldBloodMoon) {
                    Bukkit.broadcastMessage(config.getBloodMoonBroadcastStart().replace("{world}", world.getName()));
                } else {
                    Bukkit.broadcastMessage(config.getBloodMoonBroadcastEnd().replace("{world}", world.getName()));
                }
                changed = true;
            }
        }

        if (changed) {
            markDirty();
            refreshDisplays();
        }
    }

    private boolean shouldActivateBloodMoon(World world, WorldSeasonState state) {
        if (!config.isBloodMoonEnabled()) {
            return false;
        }
        long time = world.getTime();
        boolean isNight = time >= 13000L && time <= 23000L;
        if (!isNight) {
            return false;
        }
        long dayCounter = state.getWorldDayCounter();
        boolean eclipseDay = dayCounter > 0 && dayCounter % config.getEclipseIntervalDays() == 0;
        if (!eclipseDay) {
            return false;
        }
        if (!config.isRequireFullMoon()) {
            return true;
        }
        return (world.getFullTime() / 24000L) % 8L == 0L;
    }

    private void updateBossBars() {
        for (World world : Bukkit.getWorlds()) {
            if (!config.isWorldEnabled(world) || !config.isBossbarEnabled()) {
                continue;
            }
            BossBar bossBar = bloodMoonBars.computeIfAbsent(world.getName(), key -> Bukkit.createBossBar(ChatColor.DARK_RED + "Blood Moon", BarColor.RED, BarStyle.SOLID));
            if (isBloodMoonActive(world)) {
                bossBar.setVisible(true);
                bossBar.setTitle(ChatColor.DARK_RED + "Blood Moon - " + world.getName());
                for (Player player : world.getPlayers()) {
                    if (!bossBar.getPlayers().contains(player)) {
                        bossBar.addPlayer(player);
                    }
                }
            } else {
                bossBar.removeAll();
                bossBar.setVisible(false);
            }
        }
    }

    private void updateSidebars() {
        if (!config.isSidebarEnabled()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            if (!config.isWorldEnabled(world)) {
                continue;
            }
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective(SIDEBAR_OBJECTIVE, Criteria.DUMMY, ChatColor.GOLD + "Season Status");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.getScore(ChatColor.YELLOW + "World").setScore(6);
            objective.getScore(safe(world.getName(), ChatColor.WHITE, 5)).setScore(5);
            objective.getScore(ChatColor.GREEN + "Season").setScore(4);
            objective.getScore(safe(getSeason(world).name(), ChatColor.AQUA, 3)).setScore(3);
            objective.getScore(ChatColor.RED + "Blood Moon").setScore(2);
            objective.getScore(safe(isBloodMoonActive(world) ? "ACTIVE" : "Inactive", ChatColor.RED, 1)).setScore(1);
            player.setScoreboard(scoreboard);
        }
    }

    private String safe(String text, ChatColor color, int unique) {
        String value = color + text + ChatColor.values()[Math.min(unique, ChatColor.values().length - 1)];
        return value.length() > 40 ? value.substring(0, 40) : value;
    }

    private void multiplyAttribute(LivingEntity entity, Attribute attribute, double multiplier, boolean healToMax) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        double base = instance.getBaseValue();
        double target = base * multiplier;
        if (instance.getValue() < target) {
            instance.setBaseValue(target);
            if (healToMax) {
                entity.setHealth(Math.min(target, entity.getAttribute(Attribute.MAX_HEALTH).getValue()));
            }
        }
    }

    private WorldSeasonState getState(World world) {
        return worldStates.computeIfAbsent(world.getName(), key -> new WorldSeasonState());
    }

    private void markDirty() {
        this.dirty = true;
    }

    private void flush() {
        if (!dirty) {
            return;
        }
        storage.save(worldStates);
        dirty = false;
    }
}
