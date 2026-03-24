package fr.denisd3d.mc2discord.fabric.events;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionResult;

public interface CommandExecuteCallback {
    Event<CommandExecuteCallback> EVENT = EventFactory.createArrayBacked(CommandExecuteCallback.class, (listeners) -> (parseResults) -> {
        for (CommandExecuteCallback listener : listeners) {
            InteractionResult result = listener.onCommandExecute(parseResults);

            if(result != InteractionResult.PASS) {
                return result;
            }
        }

        return InteractionResult.PASS;
    });

    InteractionResult onCommandExecute(ParseResults<CommandSourceStack> parseResults);
}