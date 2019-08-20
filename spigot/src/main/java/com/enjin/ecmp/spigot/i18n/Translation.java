package com.enjin.ecmp.spigot.i18n;

import com.enjin.ecmp.spigot.util.TextUtil;

public enum Translation {

    COMMAND_ROOT_DESCRIPTION("Show information about the plugin and authors."),

    COMMAND_BALANCE_DESCRIPTION("Show wallet address, eth, enj, and token balances."),

    COMMAND_HELP_DESCRIPTION("Show list of commands with their usage."),

    COMMAND_LINK_DESCRIPTION("Show linking code or linked address."),

    COMMAND_SEND_DESCRIPTION("Send the held token to another player."),

    COMMAND_TRADE_DESCRIPTION("Show help for trade sub-commands."),
    COMMAND_TRADE_INVITE_DESCRIPTION("Invite a player to trade tokens."),
    COMMAND_TRADE_ACCEPT_DESCRIPTION("Accept another player's trade invite."),
    COMMAND_TRADE_DECLINE_DESCRIPTION("Decline another player's trade invite."),

    COMMAND_UNLINK_DESCRIPTION("Removes link between wallet and player account."),

    COMMAND_WALLET_DESCRIPTION("Open your wallet inventory to view, check out, and return items.");

    private String path;
    private String translation;

    Translation(String path, String translation) {
        this.path = path;
        this.translation = translation;
    }

    Translation(String translation) {
        this.path = this.name().replace('_', '.');
        if (this.path.startsWith("."))
            this.path = "root" + path;
        this.translation = translation;
    }

    public String path() {
        return path;
    }

    public String translation() {
        return translation;
    }

    @Override
    public String toString() {
        return TextUtil.colorize(translation());
    }
}
