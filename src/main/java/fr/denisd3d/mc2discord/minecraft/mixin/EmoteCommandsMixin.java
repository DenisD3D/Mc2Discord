package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.brigadier.context.CommandContext;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.minecraft.Entities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmoteCommands.class)
public class EmoteCommandsMixin {

    @SuppressWarnings({"target", "Duplicates"})
    //? if >=26.1 {
    @Inject(method = {"lambda$register$1"}, at = @At("HEAD"))
    //?} else {
    /*@Inject(method = {"lambda$register$0", "method_43645"}, at = @At("HEAD"))
    *///?}
    private static void execute(CommandContext<CommandSourceStack> c, PlayerChatMessage message, CallbackInfo ci) {
        if (M2DUtils.isNotConfigured())
            return;

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains("me"))
            return;

        CommandSourceStack source = c.getSource();
        ServerPlayer serverPlayer = null;
        serverPlayer = source.getPlayer();

        String messageContent = ChatType.bind(ChatType.EMOTE_COMMAND, source).decorate(message.decoratedContent()).getString();
        if (serverPlayer != null) {
            MinecraftEvents.onMinecraftChatMessageEvent(messageContent, Entities.player(serverPlayer)); // Ignores hidden players
        } else {
            MessageManager.sendInfoMessage("relayed_command", messageContent).subscribe();
        }
    }
}
