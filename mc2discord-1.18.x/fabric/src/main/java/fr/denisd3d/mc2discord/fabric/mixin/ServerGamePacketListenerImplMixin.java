package fr.denisd3d.mc2discord.fabric.mixin;

import fr.denisd3d.mc2discord.fabric.events.ServerChatCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at = @At("HEAD"))
    private void execute(TextFilter.FilteredText filteredText, CallbackInfo ci) {
        String s = filteredText.getRaw();
        if (!s.startsWith("/")) {
            ServerChatCallback.EVENT.invoker().onServerChatMessage(s, this.player);
        }
    }
}