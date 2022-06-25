package ml.denisd3d.mc2discord.forge.mixin.core;

import ml.denisd3d.mc2discord.forge.EnvGenerator;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.common.ICrashCallable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReportExtender.class)
public class MixinCrashReporterExtender {
    @Inject(method = "registerCrashCallable(Lnet/minecraftforge/fml/common/ICrashCallable;)V", at = @At("HEAD"), remap = false)
    private static void registerCrashCallable(ICrashCallable callable, CallbackInfo ci) {
        EnvGenerator.crashCallables.add(callable);
    }
}
