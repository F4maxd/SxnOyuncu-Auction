package me.f4max.xyz;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.f4max.xyz.auction.Auction;
import me.f4max.xyz.auction.AuctionManager;
import me.f4max.xyz.auction.command.AuctionCommand;
import me.f4max.xyz.listener.AuctionClickListener;
import me.f4max.xyz.listener.AuctionMenuListener;
import me.f4max.xyz.listener.AuctionPacketListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class AuctionPlugin extends JavaPlugin {

    private AuctionManager auctionManager;
    private ProtocolManager protocolManager;
    private Economy economy;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault yok");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        auctionManager = new AuctionManager();
        loadAuctions();

        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new AuctionPacketListener(this, auctionManager, economy));

        getCommand("ihale").setExecutor(new AuctionCommand(auctionManager, economy));
        getServer().getPluginManager().registerEvents(new AuctionClickListener(auctionManager), this);
        getServer().getPluginManager().registerEvents(new AuctionMenuListener(auctionManager, getDataFolder()), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            auctionManager.getAuctions().stream()
                    .filter(Auction::isExpired)
                    .forEach(auction -> {
                        Player owner = Bukkit.getPlayerExact(auction.getAuctionOwner());

                        if (auction.getLastBidder().isEmpty()) {
                            if (owner != null && owner.isOnline()) {
                                owner.getInventory().addItem(auction.getItem());
                                owner.sendMessage(ChatColor.YELLOW + "İhalenize teklif gelmedi, eşyanız geri verildi.");
                            }
                        } else {
                            Player winner = Bukkit.getPlayerExact(auction.getLastBidder());
                            if (winner != null && winner.isOnline() && economy.getBalance(winner) >= auction.getLastBidderBid()) {
                                economy.withdrawPlayer(winner, auction.getLastBidderBid());
                                economy.depositPlayer(owner, auction.getLastBidderBid());
                                winner.getInventory().addItem(auction.getItem());
                                winner.sendMessage(ChatColor.GREEN + "İhaleyi aldın.");

                                if (owner != null && owner.isOnline()) {
                                    owner.sendMessage(ChatColor.GREEN + "İhaleniz satıldı ve hesabınıza " + auction.getLastBidderBid() + " yatırıldı!");
                                }

                                File historyDir = new File(getDataFolder(), "history");
                                historyDir.mkdirs();

                                File historyFile = new File(historyDir, Bukkit.getOfflinePlayer(auction.getAuctionOwner()).getUniqueId() + ".yml");
                                YamlConfiguration historyConfig = YamlConfiguration.loadConfiguration(historyFile);

                                historyConfig.set(auction.getAuctionId() + ".item", auction.getItem());
                                historyConfig.set(auction.getAuctionId() + ".finalBid", auction.getLastBidderBid());
                                historyConfig.set(auction.getAuctionId() + ".winner", auction.getLastBidder());

                                try {
                                    historyConfig.save(historyFile);
                                } catch (IOException e) {
                                    getLogger().severe("İhale geçmişi kaydedilirken hata: " + e.getMessage());
                                }
                            } else if (owner != null && owner.isOnline()) {
                                owner.getInventory().addItem(auction.getItem());
                                owner.sendMessage(ChatColor.RED + "Kazananın bakiyesi yetersiz, eşya geri verildi.");
                            }
                        }

                        Bukkit.broadcastMessage(ChatColor.RED + "İhale sona erdi: " + auction.getAuctionId() +
                                ". Kazanan: " + (auction.getLastBidder().isEmpty() ? "Teklif yok" : auction.getLastBidder() + " (" + auction.getLastBidderBid() + ")"));
                    });

            auctionManager.removeExpiredAuctions();
        }, 20L, 20L);
    }

    private void loadAuctions() {
        File file = new File(getDataFolder(), "auctions.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("auctions")) return;

        for (String auctionId : config.getConfigurationSection("auctions").getKeys(false)) {
            String path = "auctions." + auctionId;
            Auction auction = new Auction(
                    auctionId,
                    config.getString(path + ".owner"),
                    config.getDouble(path + ".startBid"),
                    config.getItemStack(path + ".item"),
                    config.getLong(path + ".remainingMillis")
            );
            auction.setLastBidder(config.getString(path + ".lastBidder"));
            auction.setLastBidderBid(config.getDouble(path + ".lastBidderBid"));
            auctionManager.add(auction);
        }
    }

    @Override
    public void onDisable() {
        saveAuctions();
    }

    private void saveAuctions() {
        File file = new File(getDataFolder(), "auctions.yml");
        YamlConfiguration config = new YamlConfiguration();

        auctionManager.getAuctions().forEach(auction -> {
            String path = "auctions." + auction.getAuctionId();
            config.set(path + ".owner", auction.getAuctionOwner());
            config.set(path + ".startBid", auction.getStartBid());
            config.set(path + ".lastBidder", auction.getLastBidder());
            config.set(path + ".lastBidderBid", auction.getLastBidderBid());
            config.set(path + ".remainingMillis", auction.getRemainingTimeMillis());
            config.set(path + ".item", auction.getItem());
        });

        try {
            config.save(file);
        } catch (IOException e) {
            getLogger().severe("İhaleler kaydedilirken hata oluştu: " + e.getMessage());
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) economy = rsp.getProvider();
        return economy != null;
    }
}
