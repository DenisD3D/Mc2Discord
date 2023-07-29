package fr.denisd3d.mc2discord.fabric.mixin;

import com.mojang.brigadier.ParseResults;
import fr.denisd3d.mc2discord.fabric.events.CommandExecuteCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void execute(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        InteractionResult result = CommandExecuteCallback.EVENT.invoker().onCommandExecute(parseResults, command);

        if(result == InteractionResult.FAIL) {
            cir.setReturnValue(0);
        }
    }
}