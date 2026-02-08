package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SayCommand.class)
public class SayCommandMixin {

    @SuppressWarnings({ "target", "Duplicates" })
    @Inject(method = { "lambda$register$0", "method_43657" }, at = @At("HEAD"))
    private static void execute(CommandContext<CommandSourceStack> c, PlayerChatMessage message,
            CallbackInfo cir) throws CommandSyntaxException {
        if (M2DUtils.isNotConfigured())
            return;

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains("say"))
            return;

        CommandSourceStack source = c.getSource();
        ServerPlayer serverPlayer = null;
        serverPlayer = source.getPlayer();

        String messageContent = ChatType.bind(ChatType.SAY_COMMAND, source).decorate(message.decoratedContent())
                .getString();
        if (serverPlayer != null) {
            PlayerEntity player = new PlayerEntity(serverPlayer.getGameProfile().name(),
                    serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().id());
            MessageManager
                    .sendChatMessage(messageContent,
                            Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_display_name, List.of(player)),
                            Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_avatar_api, List.of(player)))
                    .subscribe();
        } else {
            MessageManager.sendInfoMessage("relayed_command", messageContent).subscribe();
        }
    }
}