package ml.denisd3d.minecraft2discord.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.denisd3d.minecraft2discord.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public class DiscordCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("discord")
                        .executes(ctx ->
                        {
                            ctx.getSource().sendFeedback(new StringTextComponent(Config.SERVER.inviteLink.get()), false);
                            return 1;
                        }));
    }
}
