package fr.denisd3d.mc2discord.core.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RemoveCommand implements SlashCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        int maxAge = Math.toIntExact(event.getOption("age")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .orElse(7L));

        long maxAgeLong = maxAge * 86400000L;
        long current = Instant.now().toEpochMilli();

        int deletedFiles = 0;

        List<File> files = listFolder("logs");
        files.addAll(listFolder("crash-reports"));
        for (File file : files) {
            if (file.lastModified() + maxAgeLong < current) {
                deletedFiles++;
                Mc2Discord.LOGGER.info("Removing {}", file.toPath());
                if (!file.delete())
                    Mc2Discord.LOGGER.error("Couldn't remove {}", file.toPath());
            }
        }

        return event.reply()
                .withContent(Mc2Discord.INSTANCE.langManager.translate("commands.remove.removed", deletedFiles));
    }

    private List<File> listFolder(String folderName) {
        File folder = new File(folderName);
        File[] files = folder.listFiles();

        if (files == null)
            return new ArrayList<>();

        return List.of(files);
    }
}
