package fr.denisd3d.mc2discord.fabric.events;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionResult;

public interface CommandExecuteCallback {
    Event<CommandExecuteCallback> EVENT = EventFactory.createArrayBacked(CommandExecuteCallback.class, (listeners) -> (parseResults, command) -> {
        for (CommandExecuteCallback listener : listeners) {
            InteractionResult result = listener.onCommandExecute(parseResults, command);

            if(result != InteractionResult.PASS) {
                return result;
            }
        }

        return InteractionResult.PASS;
    });

    InteractionResult onCommandExecute(ParseResults<CommandSourceStack> parseResults, String command);
}