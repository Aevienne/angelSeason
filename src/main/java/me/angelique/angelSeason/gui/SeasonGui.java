package me.angelique.angelSeason.gui;

import me.angelique.angelSeason.AngelSeason;
import me.angelique.angelSeason.model.SeasonType;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public final class SeasonGui {

    public static final String TITLE = TextUtil.color("&3World Status");
    static final int SIZE = 27;

    private SeasonGui() {}

    public static void open(Player player, AngelSeason plugin) {
        World world = player.getWorld();
        SeasonType season = plugin.getSeasonService().getSeason(world);
        boolean bloodMoon = plugin.getSeasonService().isBloodMoonActive(world);

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        Material seasonIcon = switch (season) {
            case SPRING -> Material.PINK_TULIP;
            case SUMMER -> Material.SUNFLOWER;
            case AUTUMN -> Material.PUMPKIN;
            case WINTER -> Material.SNOWBALL;
        };
        String seasonColor = switch (season) {
            case SPRING -> "&d";
            case SUMMER -> "&e";
            case AUTUMN -> "&6";
            case WINTER -> "&b";
        };

        inv.setItem(11, item(seasonIcon, seasonColor + season.name(),
                "&7Current season for &f" + world.getName(),
                "&7Seasons auto-cycle every few hours",
                "&7Each season affects crop growth & spawns"));

        inv.setItem(13, item(bloodMoon ? Material.REDSTONE_BLOCK : Material.COAL,
                bloodMoon ? "&4Blood Moon ACTIVE" : "&7Blood Moon: Inactive",
                bloodMoon ? "&cMonsters are empowered!" : "&7Blood moons occur at night",
                bloodMoon ? "&cBuffed: HP x2, DMG x1.5, SPD x1.3" : "&7Kill mobs for bonus loot"));

        String desc = switch (season) {
            case SPRING -> "&dCrops grow faster \u2022 Sheep spawn more";
            case SUMMER -> "&eLonger days \u2022 Fishing bonus";
            case AUTUMN -> "&6Harvest bonus \u2022 Pumpkin spawns";
            case WINTER -> "&bSlow crop growth \u2022 Ice & snow";
        };
        inv.setItem(15, item(Material.KNOWLEDGE_BOOK, "&eSeason Effects",
                desc,
                "",
                "&7Use &e/season status &7for details"));

        player.openInventory(inv);
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, glass);
    }

    static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(Arrays.stream(lore).map(TextUtil::color).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    static ItemStack pane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }
}
