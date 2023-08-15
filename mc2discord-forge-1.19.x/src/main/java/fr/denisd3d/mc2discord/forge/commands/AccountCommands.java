package fr.denisd3d.mc2discord.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import fr.denisd3d.mc2discord.core.AccountManager;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.forge.MinecraftImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class AccountCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("discord")
                .then(Commands.literal("link")
                        .executes(context -> {
                            if (!Mc2Discord.INSTANCE.config.features.account_linking)
                                return 0;

                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String code = AccountManager.checkLinkedOrGenerateCode(player.getUUID());
                            if (code != null) {
                                context.getSource().sendSuccess(getLinkTextComponent(code), false);
                            } else {
                                context.getSource().sendFailure(MinecraftImpl.convertToComponent(Mc2Discord.INSTANCE.config.account.messages.link_error_already.asString()));
                            }
                            return 1;
                        }))
                .then(Commands.literal("unlink")
                        .executes(context -> {
                            if (!Mc2Discord.INSTANCE.config.features.account_linking)
                                return 0;

                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if (AccountManager.unlinkAccount(player.getUUID())) {
                                if (Mc2Discord.INSTANCE.config.account.force_link) {
                                    player.connection.disconnect(MinecraftImpl.convertToComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful.asString()));
                                } else {
                                    context.getSource().sendSuccess(MinecraftImpl.convertToComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful.asString()), false);
                                }
                            } else {
                                context.getSource().sendFailure(MinecraftImpl.convertToComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_error.asString()));
                            }

                            return 1;
                        })));
    }


    public static Component getLinkTextComponent(String code) {
        Map<String, MutableComponent> replacements = new HashMap<>();
        replacements.put("command", Component.literal("!code " + code)
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "!code " + code))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                        .withColor(ChatFormatting.BLUE)
                        .withUnderlined(true)));

        return MinecraftImpl.convertToComponent(Entity.replace(Mc2Discord.INSTANCE.config.account.messages.link_get_code.asString()), replacements);
    }
}
