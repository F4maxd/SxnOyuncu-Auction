package me.f4max.xyz.auction;

import org.bukkit.inventory.ItemStack;

public class Auction {
    private final String auctionId;
    private final String auctionOwner;
    private final double startBid;
    private String lastBidder;
    private double lastBidderBid;
    private final ItemStack item;
    private final long auctionEndTime;

    public Auction(String auctionId, String auctionOwner, double startBid, ItemStack item, long remainingMillis) {
        this.auctionId = auctionId;
        this.auctionOwner = auctionOwner;
        this.startBid = startBid;
        this.lastBidder = "";
        this.lastBidderBid = 0.0;
        this.item = item;
        this.auctionEndTime = System.currentTimeMillis() + remainingMillis;
    }


    public long getRemainingTimeMillis() {
        return Math.max(auctionEndTime - System.currentTimeMillis(), 0);
    }


    public boolean isExpired() {
        return System.currentTimeMillis() >= auctionEndTime;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getAuctionOwner() {
        return auctionOwner;
    }

    public double getStartBid() {
        return startBid;
    }

    public String getLastBidder() {
        return lastBidder;
    }

    public void setLastBidder(String lastBidder) {
        this.lastBidder = lastBidder;
    }

    public double getLastBidderBid() {
        return lastBidderBid;
    }

    public void setLastBidderBid(double lastBidderBid) {
        this.lastBidderBid = lastBidderBid;
    }

    public ItemStack getItem() {
        return item;
    }
}
