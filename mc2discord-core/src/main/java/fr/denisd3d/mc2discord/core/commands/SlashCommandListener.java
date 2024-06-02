package fr.denisd3d.mc2discord.core.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandListener {
    private static final List<SlashCommand> commands = new ArrayList<>();

    static {
        commands.add(new ListCommand());
        commands.add(new DisplayFileCommand());
        commands.add(new RemoveCommand());
    }

    public static Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .flatMap(command -> command.handle(event));
    }
}
