package ml.denisd3d.mc2discord.forge.mixin.core;

import com.mojang.brigadier.context.CommandContext;
import ml.denisd3d.mc2discord.core.M2DUtils;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.impl.TellRawCommand;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TellRawCommand.class)
public class TellRawCommandMixin {
    @SuppressWarnings("target")
    @Inject(method = {"lambda$register$1(Lcom/mojang/brigadier/context/CommandContext;)I", "func_198819_a(Lcom/mojang/brigadier/context/CommandContext;)I"}, at = @At("HEAD"), remap = false)
    private static void lambda(CommandContext<CommandSource> commandContext, CallbackInfoReturnable<Integer> ci) {
        if (Mc2Discord.INSTANCE.config.misc.relay_say_me_tellraw_command && M2DUtils.canHandleEvent() && commandContext.getInput()
                .split(" ")[1].equals("@a")) {
            ITextComponent component = ComponentArgument.getComponent(commandContext, "message");
            Mc2Discord.INSTANCE.messageManager.sendInfoMessage(component.getString());
        }
    }

//    @Debug(print = true)
//    @Shadow
//    public static void register(CommandDispatcher<CommandSource> dispatcher) {
//        throw new AbstractMethodError("register");
//    }

    // To get name for the lambda :
    // Comment out the lambda method
    // uncomment the register command
    // run the server with -Dmixin.debug=true
    // get mapped name of the lambda (compare with bytecode in idea to find)
}
