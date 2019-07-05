package ml.denis3d.minecraft2discord;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public class DiscordCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("discord")
                        .executes(ctx ->
                        {
                            ctx.getSource().sendFeedback(new StringTextComponent(Config.SERVER.discordInviteLink.get()), true);
                            return 1;
                        }));
    }
}
