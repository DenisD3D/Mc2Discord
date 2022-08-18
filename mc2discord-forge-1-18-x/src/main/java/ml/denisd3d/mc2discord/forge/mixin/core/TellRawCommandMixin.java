package ml.denisd3d.mc2discord.forge.mixin.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import ml.denisd3d.mc2discord.core.M2DUtils;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.TellRawCommand;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TellRawCommand.class)
public class TellRawCommandMixin {
    @SuppressWarnings("target")
    @Inject(method = {"lambda$register$1(Lcom/mojang/brigadier/context/CommandContext;)I", "m_139065_(Lcom/mojang/brigadier/context/CommandContext;)I"}, at = @At("HEAD"), remap = false)
    private static void lambda(CommandContext<CommandSourceStack> commandContext, CallbackInfoReturnable<Integer> ci) {
        if (Mc2Discord.INSTANCE.config.misc.relay_say_me_command && M2DUtils.canHandleEvent() && commandContext.getInput()
                .split(" ")[1].equals("@a")) {
            Component component = ComponentArgument.getComponent(commandContext, "message");
            Mc2Discord.INSTANCE.messageManager.sendInfoMessage(component.getString());
        }
    }

//    @Debug(print = true)
//    @Shadow
//    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
//        throw new AbstractMethodError("register");
//    }

    // To get name for the lambda :
    // Comment out the lambda method
    // uncomment the register command
    // run the server with -Dmixin.debug=true
    // get mapped name of the lambda (compare with bytecode in idea to find)
}
