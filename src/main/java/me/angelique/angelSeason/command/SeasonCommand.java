package me.angelique.angelSeason.command;

import me.angelique.angelSeason.AngelSeason;
import me.angelique.angelSeason.config.PluginConfig;
import me.angelique.angelSeason.gui.SeasonGui;
import me.angelique.angelSeason.model.SeasonType;
import me.angelique.angelSeason.service.SeasonService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SeasonCommand implements CommandExecutor, TabCompleter {

    private final AngelSeason plugin;
    private final SeasonService seasonService;
    private final PluginConfig config;

    public SeasonCommand(AngelSeason plugin, SeasonService seasonService, PluginConfig config) {
        this.plugin = plugin;
        this.seasonService = seasonService;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                SeasonGui.open(player, plugin);
            } else {
                sender.sendMessage("/season <status|set|next|bloodmoon|reload>");
            }
            return true;
        }

        if ("status".equalsIgnoreCase(args[0])) {
            World world = sender instanceof Player player ? player.getWorld() : Bukkit.getWorlds().getFirst();
            sender.sendMessage(config.getPrefix() + config.getSeasonStatus()
                    .replace("{world}", world.getName())
                    .replace("{season}", seasonService.getSeason(world).name())
                    .replace("{bloodmoon}", seasonService.isBloodMoonActive(world) ? "ACTIVE" : "Inactive"));
            return true;
        }

        if (!sender.hasPermission("angelseason.admin")) {
            sender.sendMessage(config.getPrefix() + "No permission.");
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPlugin();
            sender.sendMessage(config.getPrefix() + config.getReload());
            return true;
        }

        if ("set".equalsIgnoreCase(args[0]) && args.length >= 3) {
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(config.getPrefix() + "World not found.");
                return true;
            }
            try {
                SeasonType seasonType = SeasonType.valueOf(args[2].toUpperCase());
                seasonService.setSeason(world, seasonType);
                sender.sendMessage(config.getPrefix() + config.getSeasonSet()
                        .replace("{world}", world.getName())
                        .replace("{season}", seasonType.name()));
            } catch (IllegalArgumentException exception) {
                sender.sendMessage(config.getPrefix() + "Invalid season.");
            }
            return true;
        }

        if ("next".equalsIgnoreCase(args[0]) && args.length >= 2) {
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(config.getPrefix() + "World not found.");
                return true;
            }
            SeasonType next = seasonService.advanceSeason(world);
            sender.sendMessage(config.getPrefix() + config.getSeasonNext()
                    .replace("{world}", world.getName())
                    .replace("{season}", next.name()));
            return true;
        }

        if ("bloodmoon".equalsIgnoreCase(args[0]) && args.length >= 3) {
            World world = Bukkit.getWorld(args[2]);
            if (world == null) {
                sender.sendMessage(config.getPrefix() + "World not found.");
                return true;
            }
            if ("start".equalsIgnoreCase(args[1])) {
                seasonService.setBloodMoon(world, true);
                sender.sendMessage(config.getPrefix() + "Blood Moon started in " + world.getName());
                return true;
            }
            if ("stop".equalsIgnoreCase(args[1])) {
                seasonService.setBloodMoon(world, false);
                sender.sendMessage(config.getPrefix() + "Blood Moon stopped in " + world.getName());
                return true;
            }
        }

        sender.sendMessage(config.getPrefix() + "/season status");
        sender.sendMessage(config.getPrefix() + "/season set <world> <season>");
        sender.sendMessage(config.getPrefix() + "/season next <world>");
        sender.sendMessage(config.getPrefix() + "/season bloodmoon <start|stop> <world>");
        sender.sendMessage(config.getPrefix() + "/season reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("status");
            suggestions.add("set");
            suggestions.add("next");
            suggestions.add("bloodmoon");
            suggestions.add("reload");
        } else if (args.length == 2 && ("set".equalsIgnoreCase(args[0]) || "next".equalsIgnoreCase(args[0]))) {
            Bukkit.getWorlds().forEach(world -> suggestions.add(world.getName()));
        } else if (args.length == 3 && "set".equalsIgnoreCase(args[0])) {
            for (SeasonType seasonType : SeasonType.values()) {
                suggestions.add(seasonType.name());
            }
        } else if (args.length == 2 && "bloodmoon".equalsIgnoreCase(args[0])) {
            suggestions.add("start");
            suggestions.add("stop");
        } else if (args.length == 3 && "bloodmoon".equalsIgnoreCase(args[0])) {
            Bukkit.getWorlds().forEach(world -> suggestions.add(world.getName()));
        }
        return suggestions;
    }
}
