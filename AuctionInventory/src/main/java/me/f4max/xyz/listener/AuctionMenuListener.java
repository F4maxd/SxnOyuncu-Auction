package me.f4max.xyz.listener;

import me.f4max.xyz.auction.AuctionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Arrays;

public class AuctionMenuListener implements Listener {

    private final AuctionManager auctionManager;
    private final File dataFolder;

    public AuctionMenuListener(AuctionManager auctionManager, File dataFolder) {
        this.auctionManager = auctionManager;
        this.dataFolder = dataFolder;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.equals("İhale Menüsü") || title.equals("Mevcut İhaleler") || title.equals("İhale Geçmişim")) {
            event.setCancelled(true);
        }

        Player player = (Player) event.getWhoClicked();

        if (title.equals("İhale Menüsü")) {
            if (event.getSlot() == 15) {
                Inventory gui = Bukkit.createSingleInventory(null, "Mevcut İhaleler", 121);
                auctionManager.getAuctions().forEach(auction -> {
                    ItemStack item = auction.getItem().clone();
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Başlatan: " + ChatColor.WHITE + auction.getAuctionOwner(),
                            ChatColor.YELLOW + "Auction ID: " + ChatColor.WHITE + auction.getAuctionId(),
                            ChatColor.YELLOW + "Başlangıç Fiyatı: " + ChatColor.WHITE + auction.getStartBid(),
                            ChatColor.YELLOW + "Son Teklif: " + ChatColor.WHITE + auction.getLastBidderBid(),
                            ChatColor.YELLOW + "Kalan Süre: " + ChatColor.WHITE + auction.getRemainingTimeMillis() / 1000 + " saniye"
                    ));
                    item.setItemMeta(meta);
                    gui.addItem(item);
                });
                gui.setItem(120, getBackArrow());
                player.openInventory(gui);
            } else if (event.getSlot() == 17) {
                Inventory gui = Bukkit.createSingleInventory(null, "İhale Geçmişim", 121);
                File historyFile = new File(dataFolder, "history/" + player.getUniqueId() + ".yml");
                if (!historyFile.exists()) {
                    player.sendMessage(ChatColor.RED + "Geçmişinde hiç ihale bulunamadı.");
                    return;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
                config.getKeys(false).forEach(auctionId -> {
                    ItemStack item = config.getItemStack(auctionId + ".item");
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Son Fiyat: " + ChatColor.WHITE + config.getDouble(auctionId + ".finalBid"),
                            ChatColor.YELLOW + "Kazanan: " + ChatColor.WHITE + config.getString(auctionId + ".winner")
                    ));
                    item.setItemMeta(meta);
                    gui.addItem(item);
                });
                gui.setItem(120, getBackArrow());
                player.openInventory(gui);
            }
        } else if ((title.equals("Mevcut İhaleler") || title.equals("İhale Geçmişim")) && event.getSlot() == 120) {
            player.performCommand("ihale");
        }
    }

    private ItemStack getBackArrow() {
        ItemStack backArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = backArrow.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Geri Dön");
        meta.setLore(Arrays.asList(ChatColor.WHITE + "İhale Menüsüne geri dön."));
        backArrow.setItemMeta(meta);
        return backArrow;
    }
}
