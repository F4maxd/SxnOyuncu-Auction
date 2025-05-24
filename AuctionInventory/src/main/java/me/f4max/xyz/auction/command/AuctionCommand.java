package me.f4max.xyz.auction.command;

import me.f4max.xyz.auction.Auction;
import me.f4max.xyz.auction.AuctionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuctionCommand implements CommandExecutor {

    private final AuctionManager auctionManager;
    private final Economy economy;

    public AuctionCommand(AuctionManager auctionManager, Economy economy) {
        this.auctionManager = auctionManager;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            Inventory gui = Bukkit.createSingleInventory(null, "İhale Menüsü", 33);

            ItemStack currentAuctionsItem = new ItemStack(Material.GOLD_INGOT);
            ItemMeta currentMeta = currentAuctionsItem.getItemMeta();
            currentMeta.setDisplayName(ChatColor.GREEN + "Mevcut ihaleler");
            currentMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Burada aktif ihaleleri",
                    ChatColor.GRAY + "görebilirsin.",
                    "",
                    ChatColor.YELLOW + "Açmak için tıkla"
            ));

            currentAuctionsItem.setItemMeta(currentMeta);
            gui.setItem(15, currentAuctionsItem);

            ItemStack currentİnfoItem = new ItemStack(Material.BOOK);
            ItemMeta infoMeta = currentİnfoItem.getItemMeta();
            infoMeta.setDisplayName(ChatColor.YELLOW + "Nasıl ihale başlatırım?");
            infoMeta.setLore(Arrays.asList(
                    ChatColor.WHITE + "/ihale başlat <para>",
                    ChatColor.GRAY + "komutunu kullanarak",
                    ChatColor.GRAY + "elindeki eşya için",
                    ChatColor.GRAY + "ihale başlatabilirsin."
            ));

            currentİnfoItem.setItemMeta(infoMeta);
            gui.setItem(27, currentİnfoItem);

            ItemStack auctionHistoryItem = new ItemStack(Material.BOOK_AND_QUILL);
            ItemMeta historyMeta = auctionHistoryItem.getItemMeta();
            historyMeta.setDisplayName(ChatColor.GREEN + "İhale Geçmişi");
            historyMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "oluşturduğun tüm",
                    ChatColor.GRAY + "ihaleleri burada",
                    ChatColor.GRAY + "bulabilirsin.",
                    "",
                    ChatColor.YELLOW + "Açmak için tıkla"
            ));

            auctionHistoryItem.setItemMeta(historyMeta);
            gui.setItem(17, auctionHistoryItem);

            player.openInventory(gui);
            return true;
        }


        if (args[0].equalsIgnoreCase("başlat") && args.length == 2) {
            double startBid;
            try {
                startBid = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.DARK_RED + "Geçerli fiyat giriniz.");
                return true;
            }

            ItemStack handItem = player.getItemInHand();
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.DARK_RED + "Elinde eşya yok!");
                return true;
            }

            Auction auction = auctionManager.createAuction(player.getName(), startBid, handItem.clone());

            if (auction == null) {
                player.sendMessage(ChatColor.DARK_RED + "Zaten aktif bir ihaleniz var! Yeni ihale başlatmadan önce mevcut ihalenizin sona ermesini bekleyin.");
                return true;
            }

            String itemName = handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName()
                    ? handItem.getItemMeta().getDisplayName()
                    : handItem.getType().toString();

            Bukkit.broadcastMessage(ChatColor.GOLD + "Yeni İhale Başlatıldı: "
                    + ChatColor.AQUA + itemName
                    + ChatColor.YELLOW + " | Fiyat: "
                    + ChatColor.GREEN + startBid);

            player.sendMessage(ChatColor.GREEN + "İhale başarıyla başlatıldı!");
            player.setItemInHand(null);
            return true;
        }

        player.sendMessage(ChatColor.DARK_RED + "Kullanım: /ihale başlat <fiyat>");
        return true;
    }
}