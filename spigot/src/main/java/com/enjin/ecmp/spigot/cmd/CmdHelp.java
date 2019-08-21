package com.enjin.ecmp.spigot.cmd;

import com.enjin.ecmp.spigot.SpigotBootstrap;
import com.enjin.ecmp.spigot.enums.Permission;
import com.enjin.ecmp.spigot.i18n.Translation;
import org.bukkit.command.CommandSender;

public class CmdHelp extends EnjCommand {

    public CmdHelp(SpigotBootstrap bootstrap, EnjCommand parent) {
        super(bootstrap, parent);
        this.aliases.add("help");
        this.aliases.add("h");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.ANY)
                .withPermission(Permission.CMD_HELP)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        CommandSender sender = context.sender;
        showHelp(sender, parent.get());
    }

    private void showHelp(CommandSender sender, EnjCommand command) {
        command.showHelp(sender);
        command.subCommands.forEach(c -> showHelp(sender, c));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_HELP_DESCRIPTION;
    }

}
