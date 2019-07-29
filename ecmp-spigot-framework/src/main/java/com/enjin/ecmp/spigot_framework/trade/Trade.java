package com.enjin.ecmp.spigot_framework.trade;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class Trade {

    private UUID playerOneUuid;
    private List<ItemStack> playerOneOffer;

    private UUID playerTwoUuid;
    private List<ItemStack> playerTwoOffer;

    private String tradeId;

    public Trade(UUID playerOneUuid, List<ItemStack> playerOneOffer, UUID playerTwoUuid, List<ItemStack> playerTwoOffer) {
        this.playerOneUuid = playerOneUuid;
        this.playerOneOffer = playerOneOffer;
        this.playerTwoUuid = playerTwoUuid;
        this.playerTwoOffer = playerTwoOffer;
    }

    public UUID getPlayerOneUuid() {
        return playerOneUuid;
    }

    public List<ItemStack> getPlayerOneOffer() {
        return playerOneOffer;
    }

    public UUID getPlayerTwoUuid() {
        return playerTwoUuid;
    }

    public List<ItemStack> getPlayerTwoOffer() {
        return playerTwoOffer;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "playerOneUuid=" + playerOneUuid +
                ", playerOneOffer=" + playerOneOffer +
                ", playerTwoUuid=" + playerTwoUuid +
                ", playerTwoOffer=" + playerTwoOffer +
                '}';
    }
}
