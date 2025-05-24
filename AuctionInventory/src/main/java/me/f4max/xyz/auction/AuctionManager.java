package me.f4max.xyz.auction;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuctionManager {

    private final Map<String, Auction> auctions = new HashMap<>();

    public Auction createAuction(String auctionOwner, double startBid, ItemStack item) {
        if (hasActiveAuction(auctionOwner)) {
            return null;
        }

        Auction auction = new Auction(UUID.randomUUID().toString(), auctionOwner, startBid, item, 60000);
        auctions.put(auction.getAuctionId(), auction);
        return auction;
    }

    public boolean hasActiveAuction(String auctionOwner) {
        return auctions.values().stream()
                .anyMatch(auction -> auction.getAuctionOwner().equalsIgnoreCase(auctionOwner) && !auction.isExpired());
    }

    public Collection<Auction> getAuctions() {
        return auctions.values();
    }

    public void add(Auction auction) {
        auctions.put(auction.getAuctionId(), auction);
    }

    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    public void removeExpiredAuctions() {
        auctions.values().removeIf(Auction::isExpired);
    }

    public boolean auctionExists(String auctionId) {
        return auctions.containsKey(auctionId);
    }
}
