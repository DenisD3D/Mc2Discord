package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.brigadier.context.CommandContext;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SayCommand.class)
public class SayCommandMixin {

    @SuppressWarnings({"target", "Duplicates"})
    @Inject(method = {"lambda$register$1", "method_43657"}, at = @At("HEAD"))
    private static void execute(CommandContext<CommandSourceStack> commandContext, PlayerChatMessage message, CallbackInfo ci) {
        if (M2DUtils.isNotConfigured()) return;

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains("say")) return;

        ServerPlayer serverPlayer = commandContext.getSource().getPlayer();
        String messageContent = ChatType.bind(ChatType.SAY_COMMAND, commandContext.getSource()).decorate(message.decoratedContent()).getString();
        if (serverPlayer != null) {
            PlayerEntity player = new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId());
            MessageManager.sendChatMessage(messageContent, Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_display_name, List.of(player)), Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_avatar_api, List.of(player))).subscribe();
        } else {
            MessageManager.sendInfoMessage("relayed_command", messageContent).subscribe();
        }
    }
}