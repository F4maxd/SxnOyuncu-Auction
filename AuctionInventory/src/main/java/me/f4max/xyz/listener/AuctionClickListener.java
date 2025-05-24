package me.f4max.xyz.listener;

import me.f4max.xyz.auction.Auction;
import me.f4max.xyz.auction.AuctionManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class AuctionClickListener implements Listener {

    private final AuctionManager auctionManager;

    public AuctionClickListener(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    @EventHandler
    public void onAuctionItemClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Mevcut İhaleler")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (!meta.hasLore()) {
            player.sendMessage(ChatColor.DARK_RED + "Bu item geçerli bir ihale içermiyor.");
            return;
        }

        String auctionId = null;

        for (String loreLine : meta.getLore()) {
            if (ChatColor.stripColor(loreLine).startsWith("Auction ID: ")) {
                auctionId = ChatColor.stripColor(loreLine).replace("Auction ID: ", "").trim();
                break;
            }
        }

        if (auctionId == null) {
            return;
        }

        Auction clickedAuction = auctionManager.getAuction(auctionId);
        if (clickedAuction == null) {
            player.sendMessage(ChatColor.DARK_RED + "İhale bulunamadı veya sona erdi.");
            return;
        }

        Map<String, String> extra = new HashMap<>();
        extra.put("AuctionOwner", clickedAuction.getAuctionOwner());
        extra.put("AuctionId", clickedAuction.getAuctionId());
        extra.put("StartBid", String.valueOf(clickedAuction.getStartBid()));
        extra.put("LastBidder", clickedAuction.getLastBidder());
        extra.put("LastBidderBid", String.valueOf(clickedAuction.getLastBidderBid()));

        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
                1,
                "minecraft:auctioninventory",
                new ChatComponentText("Teklif Ver"),
                77,
                extra
        );

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(packet);

        ItemStack auctionItem = CraftItemStack.asNMSCopy(clickedAuction.getItem());
        PacketPlayOutSetSlot slotPacket = new PacketPlayOutSetSlot(1, 0, auctionItem);
        entityPlayer.playerConnection.sendPacket(slotPacket);
    }
}
