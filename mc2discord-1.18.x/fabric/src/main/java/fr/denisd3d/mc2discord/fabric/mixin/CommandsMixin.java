package fr.denisd3d.mc2discord.fabric.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import fr.denisd3d.mc2discord.fabric.events.CommandExecuteCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandsMixin {
    @Final
    @Shadow
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void execute(CommandSourceStack commandSourceStack, String string, CallbackInfoReturnable<Integer> cir) {
        StringReader stringReader = new StringReader(string);
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }

        ParseResults<CommandSourceStack> parseResults = dispatcher.parse(stringReader, commandSourceStack);

        InteractionResult result = CommandExecuteCallback.EVENT.invoker().onCommandExecute(parseResults);

        if(result == InteractionResult.FAIL) {
            cir.setReturnValue(0);
        }
    }
}