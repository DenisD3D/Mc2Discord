package ml.denisd3d.mc2discord.forge.account;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class UnLinkCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        String[] strings = Mc2Discord.INSTANCE.config.account.unlink_command.trim().split(" ");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(strings[strings.length - 1]).executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (Mc2Discord.INSTANCE.m2dAccount != null) { // Should always be the case at this point
                if (Mc2Discord.INSTANCE.m2dAccount.iAccount.remove(player.getGameProfile().getId())) {
                    if (Mc2Discord.INSTANCE.config.account.force_link) {
                        player.connection.disconnect(new TextComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful));
                    } else {
                        context.getSource().sendSuccess(new TextComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful), false);
                    }
                } else {
                    context.getSource().sendFailure(new TextComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_error));
                }
            }
            return 1;
        });
        for (int i = strings.length - 2; i >= 0; i--) {
            command = Commands.literal(strings[i]).then(command);
        }
        dispatcher.register(command);
    }
}
