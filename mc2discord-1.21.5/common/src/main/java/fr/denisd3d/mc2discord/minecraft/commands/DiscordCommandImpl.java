package fr.denisd3d.mc2discord.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import fr.denisd3d.mc2discord.core.M2DCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.net.URI;

public class DiscordCommandImpl {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("discord").executes(context -> {
            context.getSource().sendSuccess(() -> Component.literal(M2DCommands.getDiscordText())
                    .withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(M2DCommands.getDiscordLink())))
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                            .withUnderlined(true)), false);
            return 1;
        }));
    }
}