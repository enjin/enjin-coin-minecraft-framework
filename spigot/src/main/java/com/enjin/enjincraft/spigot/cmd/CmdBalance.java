package com.enjin.enjincraft.spigot.cmd;

import com.enjin.enjincraft.spigot.SpigotBootstrap;
import com.enjin.enjincraft.spigot.token.TokenModel;
import com.enjin.enjincraft.spigot.enums.Permission;
import com.enjin.enjincraft.spigot.i18n.Translation;
import com.enjin.enjincraft.spigot.player.EnjPlayer;
import com.enjin.enjincraft.spigot.util.MessageUtils;
import com.enjin.enjincraft.spigot.wallet.MutableBalance;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CmdBalance extends EnjCommand {

    public CmdBalance(SpigotBootstrap bootstrap, CmdEnj parent) {
        super(bootstrap, parent);
        this.aliases.add("balance");
        this.aliases.add("bal");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_BALANCE)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        Player sender = context.player;
        EnjPlayer enjPlayer = context.enjPlayer;

        if (!enjPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_SELF.send(sender);
            return;
        }

        BigDecimal ethBalance = (enjPlayer.getEthBalance() == null)
                ? BigDecimal.ZERO
                : enjPlayer.getEthBalance();
        BigDecimal enjBalance = (enjPlayer.getEnjBalance() == null)
                ? BigDecimal.ZERO
                : enjPlayer.getEnjBalance();

        Translation.COMMAND_BALANCE_WALLETADDRESS.send(sender, enjPlayer.getEthereumAddress());
        Translation.COMMAND_BALANCE_IDENTITYID.send(sender, enjPlayer.getIdentityId());

        if (enjBalance != null)
            Translation.COMMAND_BALANCE_ENJBALANCE.send(sender, enjBalance);
        if (enjBalance != null)
            Translation.COMMAND_BALANCE_ETHBALANCE.send(sender, ethBalance);

        int itemCount = 0;
        List<String> tokenDisplays = new ArrayList<>();
        for (MutableBalance balance : enjPlayer.getTokenWallet().getBalances()) {
            if (balance == null || balance.balance() == 0)
                continue;
            TokenModel model = bootstrap.getTokenManager().getToken(balance.id());
            if (model == null)
                continue;
            itemCount++;
            Translation.COMMAND_BALANCE_TOKENDISPLAY.send(sender, itemCount, model.getDisplayName(), balance.balance());
        }

        Translation.MISC_NEWLINE.send(sender);
        if (itemCount == 0)
            Translation.COMMAND_BALANCE_NOTOKENS.send(sender);
        else
            Translation.COMMAND_BALANCE_TOKENCOUNT.send(sender, itemCount);

        tokenDisplays.forEach(l -> MessageUtils.sendString(sender, l));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_BALANCE_DESCRIPTION;
    }

}
