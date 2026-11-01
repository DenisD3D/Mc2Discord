package fr.denisd3d.mc2discord.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import fr.denisd3d.mc2discord.core.AccountManager;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.minecraft.MinecraftImpl;
import fr.denisd3d.mc2discord.minecraft.VersionCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

public class AccountCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("discord").then(Commands.literal("link").executes(context -> {
            if (!Mc2Discord.INSTANCE.config.features.account_linking)
                return 0;

            ServerPlayer player = context.getSource().getPlayerOrException();
            String code = AccountManager.checkLinkedOrGenerateCode(player.getUUID());
            if (code != null) {
                context.getSource().sendSuccess(() -> getLinkTextComponent(code), false);
            } else {
                context.getSource().sendFailure(MinecraftImpl.toComponent(Mc2Discord.INSTANCE.config.account.messages.link_error_already.pick()));
            }
            return 1;
        })).then(Commands.literal("unlink").executes(context -> {
            if (!Mc2Discord.INSTANCE.config.features.account_linking)
                return 0;

            ServerPlayer player = context.getSource().getPlayerOrException();
            if (AccountManager.unlinkAccount(player.getUUID())) {
                if (Mc2Discord.INSTANCE.config.account.force_link) {
                    player.connection.disconnect(MinecraftImpl.toComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful.pick()));
                } else {
                    context.getSource()
                            .sendSuccess(() -> MinecraftImpl.toComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful.pick()), false);
                }
            } else {
                context.getSource().sendFailure(MinecraftImpl.toComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_error.pick()));
            }

            return 1;
        })));
    }

    public static Component getLinkTextComponent(String code) {
        Component command = Component.literal("!code " + code)
                .withStyle(style -> style.withClickEvent(VersionCompat.copyToClipboard("!code " + code))
                        .withHoverEvent(VersionCompat.showText(Component.translatable("chat.copy.click")))
                        .withColor(ChatFormatting.BLUE)
                        .withUnderlined(true));

        return MinecraftImpl.toComponent(Mc2Discord.INSTANCE.config.account.messages.link_get_code.pick(), Map.of("command", command), List.of());
    }
}
