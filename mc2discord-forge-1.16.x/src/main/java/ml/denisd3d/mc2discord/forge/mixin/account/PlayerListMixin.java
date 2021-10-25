package ml.denisd3d.mc2discord.forge.mixin.account;

import com.mojang.authlib.GameProfile;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(net.minecraft.server.management.PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/util/text/ITextComponent;", at = @At(value = "HEAD"), cancellable = true)
    public void initializeConnectionToPlayer(SocketAddress p_206258_1_, GameProfile p_206258_2_, CallbackInfoReturnable<ITextComponent> cir) {
        if (Mc2Discord.INSTANCE.m2dAccount != null && Mc2Discord.INSTANCE.config.features.account_linking && Mc2Discord.INSTANCE.config.account.force_link) {
            String result = Mc2Discord.INSTANCE.m2dAccount.generateCodeOrNull(p_206258_2_, p_206258_2_.getId());
            if (result != null) {
                cir.setReturnValue(new StringTextComponent(result));
            }
        }
    }
}
