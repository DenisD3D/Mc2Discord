package fr.denisd3d.mc2discord.forge.mixin;

import com.mojang.brigadier.ParseResults;
import fr.denisd3d.mc2discord.forge.ForgeEvents;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.ServerFunctionManager;
import net.minecraftforge.event.CommandEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandFunction.CommandEntry.class)
public class CommandFunctionCommandEntryMixin {
    @Final
    @Shadow
    private ParseResults<CommandSourceStack> parse;

    @Inject(method = "execute(Lnet/minecraft/server/ServerFunctionManager;Lnet/minecraft/commands/CommandSourceStack;)I", at = @At("HEAD"))
    private void execute(ServerFunctionManager pFunctionManager, CommandSourceStack pSource, CallbackInfoReturnable<Integer> cir) {
        ForgeEvents.onCommandEvent(new CommandEvent(parse));
    }
}
