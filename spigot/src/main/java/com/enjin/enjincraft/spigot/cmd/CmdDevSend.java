package com.enjin.enjincraft.spigot.cmd;

import com.enjin.enjincraft.spigot.GraphQLException;
import com.enjin.enjincraft.spigot.NetworkException;
import com.enjin.enjincraft.spigot.SpigotBootstrap;
import com.enjin.enjincraft.spigot.cmd.arg.PlayerArgumentProcessor;
import com.enjin.enjincraft.spigot.cmd.arg.TokenDefinitionArgumentProcessor;
import com.enjin.enjincraft.spigot.token.TokenModel;
import com.enjin.enjincraft.spigot.i18n.Translation;
import com.enjin.enjincraft.spigot.player.EnjPlayer;
import com.enjin.enjincraft.spigot.util.PlayerUtils;
import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.models.request.CreateRequest;
import com.enjin.sdk.models.request.Transaction;
import com.enjin.sdk.models.request.data.SendTokenData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CmdDevSend extends EnjCommand {

    public static final int    ETH_ADDRESS_LENGTH = 42;
    public static final String ETH_ADDRESS_PREFIX = "0x";

    public CmdDevSend(SpigotBootstrap bootstrap, EnjCommand parent) {
        super(bootstrap, parent);
        this.aliases.add("devsend");
        this.requiredArgs.add("player");
        this.requiredArgs.add("token-id");
        this.requiredArgs.add("amount");
        this.requirements = CommandRequirements.builder()
                                               .withAllowedSenderTypes(SenderType.CONSOLE)
                                               .build();
    }

    @Override
    public List<String> tab(CommandContext context) {
        if (context.args.size() == 1) {
            return PlayerArgumentProcessor.INSTANCE.tab(context.sender, context.args.get(0));
        }
        if (context.args.size() == 2) {
            return TokenDefinitionArgumentProcessor.INSTANCE.tab(context.sender, context.args.get(1));
        }
        return new ArrayList<>(0);
    }

    @Override
    public void execute(CommandContext context) {
        CommandSender        sender           = context.sender;
        String               recipient        = context.args.get(0);
        String               id               = context.args.get(1);
        Optional<Player>     optionalPlayer   = PlayerArgumentProcessor.INSTANCE.parse(sender, recipient);
        Optional<TokenModel> optionalTokenDef = TokenDefinitionArgumentProcessor.INSTANCE.parse(sender, id);
        Optional<Integer>    optionalAmount;

        if (!optionalTokenDef.isPresent()) {
            Translation.COMMAND_DEVSEND_INVALIDTOKEN.send(sender);
            return;
        }

        try {
            optionalAmount = context.argToInt(2);

            if (!optionalAmount.isPresent() || optionalAmount.get() <= 0)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            Translation.COMMAND_DEVSEND_INVALIDAMOUNT.send(sender);
            return;
        }

        String targetAddr;
        if (recipient.startsWith(ETH_ADDRESS_PREFIX) && recipient.length() == ETH_ADDRESS_LENGTH) {
            targetAddr = recipient;
        } else if (PlayerUtils.isValidUserName(recipient)) {
            if (!optionalPlayer.isPresent()) {
                Translation.ERRORS_PLAYERNOTONLINE.send(sender, recipient);
                return;
            }

            Player target = optionalPlayer.get();
            if (!target.isOnline()) {
                Translation.ERRORS_PLAYERNOTONLINE.send(sender, recipient);
                return;
            }

            Optional<EnjPlayer> optionalEnjPlayer = bootstrap.getPlayerManager().getPlayer(target);
            if (!optionalEnjPlayer.isPresent()) {
                Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, recipient);
                return;
            }

            EnjPlayer targetEnjPlayer = optionalEnjPlayer.get();
            if (!targetEnjPlayer.isLinked()) {
                Translation.WALLET_NOTLINKED_OTHER.send(sender, target.getName());
                return;
            }

            targetAddr = targetEnjPlayer.getEthereumAddress();
        } else {
            Translation.ERRORS_INVALIDPLAYERNAME.send(sender, recipient);
            return;
        }

        TokenModel tokenModel = optionalTokenDef.get();
        Integer amount = optionalAmount.get();

        // TODO: Rework to be usable with NFTs.
        send(sender, bootstrap.getConfig().getDevIdentityId(), targetAddr, tokenModel.getId(), amount);
    }

    private void send(CommandSender sender, int senderId, String targetAddr, String tokenId, int amount) {
        TrustedPlatformClient client = bootstrap.getTrustedPlatformClient();
        client.getRequestService()
                .createRequestAsync(new CreateRequest()
                                .appId(client.getAppId())
                                .identityId(senderId)
                                .sendToken(SendTokenData.builder()
                                        .recipientAddress(targetAddr)
                                        .tokenId(tokenId)
                                        .value(amount)
                                        .build()),
                        networkResponse -> {
                            if (!networkResponse.isSuccess()) {
                                NetworkException exception = new NetworkException(networkResponse.code());
                                Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
                                throw exception;
                            }

                            GraphQLResponse<Transaction> graphQLResponse = networkResponse.body();
                            if (!graphQLResponse.isSuccess()) {
                                GraphQLException exception = new GraphQLException(graphQLResponse.getErrors());
                                Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
                                throw exception;
                            }

                            Translation.COMMAND_SEND_SUBMITTED.send(sender);
                });
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_DEVSEND_DESCRIPTION;
    }

}
