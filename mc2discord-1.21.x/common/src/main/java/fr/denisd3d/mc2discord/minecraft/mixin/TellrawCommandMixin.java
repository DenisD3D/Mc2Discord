package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TellRawCommand.class)
public class TellrawCommandMixin {

    @SuppressWarnings({"target", "Duplicates"})
    @Inject(method = {"lambda$register$1", "method_13777"}, at = @At("HEAD"), cancellable = true)
    private static void execute(CommandContext<CommandSourceStack> commandContext, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (M2DUtils.isNotConfigured()) return;

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains("tellraw")) return;


        if (commandContext.getInput().contains("@s") && (commandContext.getSource() == Mc2DiscordMinecraft.commandSource)) {
            cir.setReturnValue(1); // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
        } else if (!commandContext.getInput().contains("@a")) {  // Else if the target is not everyone it does not target discord
            return;
        }

        ServerPlayer serverPlayer = commandContext.getSource().getPlayer();
        String messageContent = ComponentUtils.updateForEntity(commandContext.getSource(), ComponentArgument.getComponent(commandContext, "message"), null, 0).getString();
        if (serverPlayer != null) {
            PlayerEntity player = new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId());
            MessageManager.sendChatMessage(messageContent, Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_display_name, List.of(player)), Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_avatar_api, List.of(player))).subscribe();
        } else {
            MessageManager.sendInfoMessage("relayed_command", messageContent).subscribe();
        }
    }
}