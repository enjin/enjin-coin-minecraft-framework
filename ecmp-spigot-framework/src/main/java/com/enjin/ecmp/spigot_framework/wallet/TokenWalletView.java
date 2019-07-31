package com.enjin.ecmp.spigot_framework.wallet;

import com.enjin.ecmp.spigot_framework.BasePlugin;
import com.enjin.ecmp.spigot_framework.TokenDefinition;
import com.enjin.ecmp.spigot_framework.player.EnjinCoinPlayer;
import com.enjin.minecraft_commons.spigot.ui.ClickHandler;
import com.enjin.minecraft_commons.spigot.ui.Dimension;
import com.enjin.minecraft_commons.spigot.ui.Position;
import com.enjin.minecraft_commons.spigot.ui.menu.ChestMenu;
import com.enjin.minecraft_commons.spigot.ui.menu.component.SimpleMenuComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class TokenWalletView extends ChestMenu {

    private BasePlugin plugin;
    private EnjinCoinPlayer owner;

    public TokenWalletView(BasePlugin plugin, EnjinCoinPlayer owner) {
        super("Wallet", 6);
        this.plugin = plugin;
        this.owner = owner;
        init();
    }

    private void init() {
        SimpleMenuComponent container = new SimpleMenuComponent(new Dimension(9, 6));

        List<MutableBalance> balances = owner.getTokenWallet().getBalances();

        int index = 0;
        for (MutableBalance balance : balances) {
            if (index == container.size()) break;
            if (balance.amountAvailableForWithdrawal() == 0) continue;

            TokenDefinition def = plugin.getBootstrap().getConfig().getTokens().get(balance.id());
            if (def == null) continue;
            Position position = Position.toPosition(container, index);
            ItemStack is = def.getItemStackInstance();
            container.setItem(position, is);

            addComponent(Position.of(0, 0), container);
            container.addAction(is, player -> {
                if (balance.amountAvailableForWithdrawal() > 0) {
                    balance.withdraw(1);
                    if (balance.amountAvailableForWithdrawal() == 0) {
                        container.removeItem(position);
                        container.removeAction(is);
                    }
                    refresh(player);
                }
            }, ClickType.LEFT);

            index++;
        }
    }

}