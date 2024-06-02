package fr.denisd3d.mc2discord.core.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class DisplayFileCommand implements SlashCommand {
    @Override
    public String getName() {
        return "display";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String fileName = event.getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        if (fileName.isEmpty())
            return event.reply()
                    .withContent(Mc2Discord.INSTANCE.langManager.translate("commands.display.error"));

        // Search in crash-reports first, then in logs
        InteractionApplicationCommandCallbackReplyMono event1 = searchInFolders(event, "crash-reports", fileName);
        if (event1 != null) return event1;

        event1 = searchInFolders(event, "logs", fileName);

        return Objects.requireNonNullElseGet(event1, () -> event.reply()
                .withContent(Mc2Discord.INSTANCE.langManager.translate("commands.display.no_file")));
    }

    private InteractionApplicationCommandCallbackReplyMono searchInFolders(ChatInputInteractionEvent event, String dirName, String fileName) {
        String search = searchFile(dirName, fileName);
        if (!search.isEmpty()) {
            try {
                String content = getFileContent(new File(dirName, search));
                List<String> messages = M2DUtils.breakStringInMessages(content, 2000, false);

                for (int i = 0; i < messages.size() - 1; i++)
                    event.reply()
                            .withContent(messages.get(i));
                return event.reply()
                        .withContent(messages.getLast());
            } catch (IOException e) {
                Mc2Discord.LOGGER.error("Couldn't read content of file", e);
                return event.reply()
                        .withContent(Mc2Discord.INSTANCE.langManager.translate("commands.display.error"));
            }
        }
        return null;
    }

    private String searchFile(String dirName, String fileName) {
        File crashReportsFolder = new File(dirName);
        File[] crashReportsFiles = crashReportsFolder.listFiles();
        if (crashReportsFiles == null)
            return "";
        for (File file : crashReportsFiles) {
            String name = file.getName();
            if (name.contains(fileName)) {
                // Display the content of this file
                return name;
            }
        }
        return "";
    }

    private String getFileContent(File file) throws IOException {
        return Files.readString(file.toPath());
    }
}
