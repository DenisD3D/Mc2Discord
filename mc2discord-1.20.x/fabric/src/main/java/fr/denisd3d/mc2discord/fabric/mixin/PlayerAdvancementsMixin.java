package fr.denisd3d.mc2discord.fabric.mixin;

import fr.denisd3d.mc2discord.fabric.events.PlayerCompletedAdvancementCallback;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    @SuppressWarnings("SameReturnValue")
    @Shadow
    public AdvancementProgress getOrStartProgress(AdvancementHolder advancementHolder) {
        return null;
    }

    @Inject(method = "award", at = @At("RETURN"))
    private void grantCriterion(AdvancementHolder advancementHolder, String string, CallbackInfoReturnable<Boolean> cir) {
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancementHolder);
        if (!advancementProgress.isDone()) return;
        PlayerCompletedAdvancementCallback.EVENT.invoker().onPlayerAdvancement(player, advancementHolder);
    }
}