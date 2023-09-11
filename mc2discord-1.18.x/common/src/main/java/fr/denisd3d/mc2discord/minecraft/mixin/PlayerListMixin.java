package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.authlib.GameProfile;
import fr.denisd3d.mc2discord.core.AccountManager;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.minecraft.commands.AccountCommands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    // Relay others mods messages
    @Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"))
    public void broadcastSystemMessage(Component component, ChatType type, UUID uuid, CallbackInfo ci) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.config.misc.verbose_other_mods_messages) {
            // Log the stacktrace with index in front
            Mc2Discord.LOGGER.info("Mc2Discord detected a message from another mod (verbose_other_mods_messages is enabled): " + component.getString());
            for (int i = 1; i < Thread.currentThread().getStackTrace().length; i++) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[i];
                Mc2Discord.LOGGER.info("[Stacktrace index: " + i + "] Class name: " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
            }

        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Mc2Discord.INSTANCE.config.misc.other_mods_messages.stream()
                .filter(otherModMessage -> {
                    if (otherModMessage.class_index <= 0 || otherModMessage.class_index >= stackTrace.length)
                        return false;

                    return (stackTrace[otherModMessage.class_index].getClassName() + "." + stackTrace[otherModMessage.class_index].getMethodName()).startsWith(otherModMessage.class_name);
                })
                .forEach(otherModMessage -> MessageManager.sendMessage(Collections.singletonList(otherModMessage.type), component.getString(), MessageManager.default_username, MessageManager.default_avatar).subscribe());
    }

    // Accounts
    @Inject(method = "canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;", at = @At(value = "HEAD"), cancellable = true)
    public void canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) {
        if (M2DUtils.isNotConfigured())
            return;
        if (!Mc2Discord.INSTANCE.config.features.account_linking)
            return;

        if (Mc2Discord.INSTANCE.config.account.force_link) {
            String result = AccountManager.checkLinkedOrGenerateCode(gameProfile.getId());
            if (result != null) {
                cir.setReturnValue(AccountCommands.getLinkTextComponent(result));
            }
        }
    }
}