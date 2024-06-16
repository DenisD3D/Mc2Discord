package fr.denisd3d.mc2discord.core.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListCommand implements SlashCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String filter = event.getOption("filter")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString).orElse("");

        // List files
        List<String> files = new ArrayList<>();
        String listing;
        if (filter.isEmpty()) {
            listing = "logs and crash-reports";
            files.addAll(listFolder("logs"));
            files.addAll(listFolder("crash-reports"));
        } else {
            listing = filter;
            files.addAll(listFolder(filter));
        }

        if (files.isEmpty())
            return event.reply()
                    .withContent(Mc2Discord.INSTANCE.langManager.translate("commands.list.nothing"));

        String message = "```\n%s\n```".formatted(String.join("\n", files));

        return event.reply()
                .withContent(Mc2Discord.INSTANCE.langManager.translate("commands.list.list", listing) + message);
    }

    private List<String> listFolder(String folderName) {
        List<String> fileNames = new ArrayList<>();

        File folder = new File(folderName);
        File[] files = folder.listFiles();

        if (files == null)
            return fileNames;

        for (File file : files)
            if (file.isFile())
                fileNames.add(file.getName());

        return fileNames;
    }
}
