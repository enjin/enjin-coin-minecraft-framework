package com.enjin.enjincraft.spigot.listeners;

import com.enjin.enjincraft.spigot.SpigotBootstrap;
import com.enjin.enjincraft.spigot.player.EnjPlayer;
import com.enjin.enjincraft.spigot.util.StringUtils;
import com.enjin.enjincraft.spigot.util.TokenUtils;
import com.enjin.enjincraft.spigot.wallet.MutableBalance;
import com.enjin.enjincraft.spigot.wallet.TokenWallet;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class TokenItemListener implements Listener {

    private SpigotBootstrap bootstrap;

    public TokenItemListener(SpigotBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory())
            return;

        try {
            List<ItemStack> drops = event.getDrops();
            Optional<EnjPlayer> optionalPlayer = bootstrap.getPlayerManager().getPlayer(event.getEntity());
            if (!optionalPlayer.isPresent())
                return;
            EnjPlayer player = optionalPlayer.get();
            TokenWallet wallet = player.getTokenWallet();

            for (int i = drops.size() - 1; i >= 0; i--) {
                String id = TokenUtils.getTokenID(drops.get(i));

                if (StringUtils.isEmpty(id))
                    continue;

                ItemStack is = drops.remove(i);
                MutableBalance balance = wallet.getBalance(id);
                balance.deposit(is.getAmount());
            }
        } catch (Exception ex) {
            bootstrap.log(ex);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        String id = TokenUtils.getTokenID(item.getItemStack());
        event.setCancelled(id != null);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        CraftingInventory inventory = event.getInventory();
        for (int i = inventory.getSize() - 1; i >= 0; i--) {
            ItemStack is = inventory.getItem(i);
            String id = TokenUtils.getTokenID(is);

            if (StringUtils.isEmpty(id))
                continue;

            event.setCancelled(true);
            break;
        }
    }

}
