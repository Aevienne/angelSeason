package me.angelique.angelSeason.gui;

import me.angelique.angelSeason.AngelSeason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SeasonListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(SeasonGui.TITLE)) return;
        event.setCancelled(true);
    }
}
