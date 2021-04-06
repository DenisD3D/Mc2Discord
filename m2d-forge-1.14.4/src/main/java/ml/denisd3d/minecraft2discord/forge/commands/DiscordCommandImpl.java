package ml.denisd3d.minecraft2discord.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import ml.denisd3d.minecraft2discord.core.M2DCommands;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class DiscordCommandImpl {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("discord").executes(context -> {
            context.getSource().sendFeedback(
                    new StringTextComponent(M2DCommands.getDiscordText()).setStyle(new Style()
                            .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, M2DCommands.getDiscordLink()))
                            .setColor(TextFormatting.BLUE)
                            .setUnderlined(true)),
                    false);
            return 1;
        }));
    }
}
