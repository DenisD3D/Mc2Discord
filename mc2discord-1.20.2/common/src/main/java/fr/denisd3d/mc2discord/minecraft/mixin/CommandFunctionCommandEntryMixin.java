package fr.denisd3d.mc2discord.minecraft.mixin;

import com.mojang.brigadier.ParseResults;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.ServerFunctionManager;
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
        Mc2DiscordMinecraft.onCommand(Commands.mapSource(this.parse, (ignored) -> pSource));
    }
}