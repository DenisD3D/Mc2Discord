package ml.denisd3d.mc2discord.forge.account;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkCommand {
    final static Pattern pattern = Pattern.compile("(\\$c)(.*)(\\$r)");

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        String[] strings = Mc2Discord.INSTANCE.config.account.link_command.trim().split(" ");

        LiteralArgumentBuilder<CommandSource> command = Commands.literal(strings[strings.length - 1]).executes(context -> {
            if (!Mc2Discord.INSTANCE.config.account.link_command.trim().equals(context.getInput().substring(1))) {
                context.getSource().sendFailure(new StringTextComponent("Mc2Discord settings have changed. Please use /" + Mc2Discord.INSTANCE.config.account.link_command.trim() + " instead. This command will be removed at next server restart"));
            } else {
                ServerPlayerEntity player = context.getSource().getPlayerOrException();
                if (Mc2Discord.INSTANCE.m2dAccount != null) { // Should always be the case at this point
                    String result = Mc2Discord.INSTANCE.m2dAccount.generateCodeOrNull(player.getGameProfile(), player.getGameProfile().getId());
                    if (result != null) {
                        context.getSource().sendSuccess(getCopiableTextComponent(result), false);
                    } else {
                        context.getSource().sendFailure(new StringTextComponent(Mc2Discord.INSTANCE.config.account.messages.link_error_already));
                    }
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

        TextComponent textComponent = new StringTextComponent("");
        int index = 0;
        while (matcher.find()) {
            textComponent.append(new StringTextComponent(result.substring(index, matcher.start(1))));
            textComponent.append(new StringTextComponent(matcher.group(2)).withStyle(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, matcher.group(2)))
                    .withColor(Color.fromLegacyFormat(TextFormatting.BLUE))
                    .setUnderlined(true)));
            index = matcher.end();
        }
        textComponent.append(new StringTextComponent(result.substring(index)));
        return textComponent;
    }
}
