package ml.denisd3d.mc2discord.forge.mixin.core;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.denisd3d.mc2discord.core.M2DUtils;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.SayCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SayCommand.class)
public class MixinSayCommand {
    @SuppressWarnings("target")
    @Inject(method = {"lambda$register$1(Lcom/mojang/brigadier/context/CommandContext;)I", "m_138411_(Lcom/mojang/brigadier/context/CommandContext;)I"}, at = @At("RETURN"), remap = false)
    private static void lambda(CommandContext<CommandSourceStack> commandContext, CallbackInfoReturnable<Integer> ci) throws CommandSyntaxException {
        if (Mc2Discord.INSTANCE.config.misc.relay_say_me_command && M2DUtils.canHandleEvent()) {
            Component component = MessageArgument.getMessage(commandContext, "message");
            TranslatableComponent translatableComponent = new TranslatableComponent("chat.type.announcement", commandContext.getSource().getDisplayName(), component);
            Mc2Discord.INSTANCE.messageManager.sendInfoMessage(translatableComponent.getString());
        }
    }


//    @Debug(print = true)
//    @Shadow
//    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
//        throw new AbstractMethodError("register");
//    }

    //To get name for the lambda :
    // Comment out the lambda method
    // uncomment the register command
    // run the server with -Dmixin.debug=true
    // get mapped name of the lambda (compare with bytecode in idea to find)
}
