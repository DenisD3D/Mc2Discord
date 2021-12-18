package ml.denisd3d.mc2discord.forge.account;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkCommand {
    final static Pattern pattern = Pattern.compile("(\\$c)(.*)(\\$r)");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        String[] strings = Mc2Discord.INSTANCE.config.account.link_command.trim().split(" ");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(strings[strings.length - 1]).executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (Mc2Discord.INSTANCE.m2dAccount != null) { // Should always be the case at this point
                String result = Mc2Discord.INSTANCE.m2dAccount.generateCodeOrNull(player.getGameProfile(), player.getGameProfile().getId());
                if (result != null) {
                    context.getSource().sendSuccess(getCopiableTextComponent(result), false);
                } else {
                    context.getSource().sendFailure(new TextComponent(Mc2Discord.INSTANCE.config.account.messages.link_error_already));
                }
            }
            return 1;
        });
        for (int i = strings.length - 2; i >= 0; i--) {
            command = Commands.literal(strings[i]).then(command);
        }
        dispatcher.register(command);
    }

    public static TextComponent getCopiableTextComponent(String result) {
        Matcher matcher = pattern.matcher(result);

        TextComponent textComponent = new TextComponent("");
        int index = 0;
        while (matcher.find()) {
            textComponent.append(new TextComponent(result.substring(index, matcher.start(1))));
            textComponent.append(new TextComponent(matcher.group(2)).withStyle(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, matcher.group(2)))
                    .withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                    .setUnderlined(true)));
            index = matcher.end();
        }
        textComponent.append(new TextComponent(result.substring(index)));
        return textComponent;
    }
}
