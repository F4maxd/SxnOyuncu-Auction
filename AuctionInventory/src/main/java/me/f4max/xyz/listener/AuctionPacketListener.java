package me.f4max.xyz.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.ByteBuf;
import me.f4max.xyz.AuctionPlugin;
import me.f4max.xyz.auction.Auction;
import me.f4max.xyz.auction.AuctionManager;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AuctionPacketListener extends PacketAdapter {

    private final AuctionManager auctionManager;
    private final Economy economy;

    public AuctionPacketListener(AuctionPlugin plugin, AuctionManager auctionManager, Economy economy) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.CUSTOM_PAYLOAD);
        this.auctionManager = auctionManager;
        this.economy = economy;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        String channel = event.getPacket().getStrings().read(0);
        if ("MC|Auction".equals(channel)) {
            ByteBuf byteBuf = (ByteBuf) event.getPacket().getModifier().withType(ByteBuf.class).read(0);
            PacketDataSerializer serializer = new PacketDataSerializer(byteBuf);

            serializer.c(1024);
            String auctionId = serializer.c(1024);

            double bidAmount = serializer.readDouble();

            Player player = event.getPlayer();
            Auction auction = auctionManager.getAuction(auctionId);
            if (auction == null) return;

            if (auction.getAuctionOwner().equals(player.getName())) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.DARK_RED + "Kendi ihalenize teklif veremezsiniz!");
                });
                return;
            }

            double balance = economy.getBalance(player);
            if (balance < bidAmount || bidAmount < auction.getStartBid()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.DARK_RED + "Yetersiz bakiye!");
                });
                return;
            }

            auction.setLastBidder(player.getName());
            auction.setLastBidderBid(bidAmount);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Başarıyla teklif verdiniz: " + bidAmount);
            });
        }
    }
}
