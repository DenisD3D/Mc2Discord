package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.minecraft.Entities;
import fr.denisd3d.mc2discord.minecraft.VersionCompat;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TellRawCommand.class)
public class TellrawCommandMixin {

    @SuppressWarnings({"target", "Duplicates"})
    //? if >=26.1 {
    @Inject(method = {"lambda$register$0"}, at = @At("HEAD"), cancellable = true)
    //?} elif >=1.21.11 {
    /*@Inject(method = {"lambda$register$0", "method_13777"}, at = @At("HEAD"), cancellable = true)
    *///?} else {
    /*@Inject(method = {"lambda$register$1", "method_13777"}, at = @At("HEAD"), cancellable = true)
    *///?}
    private static void execute(CommandContext<CommandSourceStack> commandContext, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (M2DUtils.isNotConfigured())
            return;

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains("tellraw"))
            return;

        if (commandContext.getInput().contains("@s") && (commandContext.getSource().source instanceof DiscordCommandSource)) {
            cir.setReturnValue(1); // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
        } else if (!commandContext.getInput().contains("@a")) { // Else if the target is not everyone it does not target discord
            return;
        }

        ServerPlayer serverPlayer = commandContext.getSource().getPlayer();
        String messageContent = VersionCompat.resolveComponentArgument(commandContext, "message").getString();
        if (serverPlayer != null) {
            MessageManager.sendChatMessage(messageContent, Entities.player(serverPlayer)).subscribe();
        } else {
            MessageManager.sendInfoMessage("relayed_command", messageContent).subscribe();
        }
    }
}
