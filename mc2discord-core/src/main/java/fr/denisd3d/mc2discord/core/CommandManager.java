package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Minecraft commands executed from Discord, with the prefix or the /mc slash command
 */
public class CommandManager {
    public static final String SLASH_COMMAND_NAME = "mc";
    private static final String COMMAND_OPTION = "command";
    private static final int MAX_CHOICES = 25; // Discord limits
    private static final int MAX_CHOICE_LENGTH = 100;
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Permissions of a Discord member to execute Minecraft commands
     * @param level The Minecraft permission level granted
     * @param commands The commands allowed in addition of the permission level
     */
    public record Permissions(int level, List<String> commands) {
        public static Permissions of(Member member) {
            Set<Snowflake> memberIds = new HashSet<>(member.getRoleIds()); // Roles ids
            memberIds.add(member.getId()); // Self id
            memberIds.add(M2DUtils.NIL_SNOWFLAKE); // Everyone id

            int level = Mc2Discord.INSTANCE.config.commands.permissions.stream()
                    .filter(commandRule -> memberIds.contains(commandRule.id))
                    .map(commandRule -> commandRule.permission_level)
                    .max(Integer::compareTo)
                    .orElse(-1);
            List<String> commands = Mc2Discord.INSTANCE.config.commands.permissions.stream()
                    .filter(commandRule -> memberIds.contains(commandRule.id))
                    .flatMap(commandRule -> commandRule.commands.stream())
                    .filter(s -> !s.isEmpty())
                    .toList();
            return new Permissions(level, commands);
        }

        /**
         * @return The permission level to execute the command with, every permission if the command is listed as allowed
         */
        public int levelFor(String command) {
            return commands.stream().anyMatch(allowed -> command.equals(allowed) || command.startsWith(allowed + " ")) ? Integer.MAX_VALUE : level;
        }
    }

    /**
     * Register the slash command in the guilds with a command channel, and remove it from the others
     */
    public static void init() {
        Set<Snowflake> commandGuilds = MessageManager.getMatchingChannels(List.of("command"))
                .stream()
                .map(channel -> Mc2Discord.INSTANCE.vars.channelCacheReverse.get(channel.channel_id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ApplicationCommandRequest> commands = Mc2Discord.INSTANCE.config.commands.slash_command ? List.of(createSlashCommand()) : List.of();

        Mc2Discord.INSTANCE.client.getRestClient()
                .getApplicationId()
                .flatMapMany(applicationId -> Mc2Discord.INSTANCE.client.getGuilds()
                        .flatMap(guild -> Mc2Discord.INSTANCE.client.getRestClient()
                                .getApplicationService()
                                .bulkOverwriteGuildApplicationCommand(applicationId, guild.getId().asLong(),
                                        commandGuilds.contains(guild.getId()) ? commands : List.of())
                                .doOnError(ClientException.class, e -> {
                                    String error = "Unable to register the /" + SLASH_COMMAND_NAME + " command in guild " + guild.getId().asString() + ": "
                                            + e.getStatus().reasonPhrase() + " (make sure the bot was invited with the applications.commands scope)";
                                    Mc2Discord.LOGGER.warn(error);
                                    Mc2Discord.INSTANCE.errors.add(error);
                                })
                                .onErrorResume(e -> Flux.empty())))
                .subscribe();
    }

    private static ApplicationCommandRequest createSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(SLASH_COMMAND_NAME)
                .description(Mc2Discord.INSTANCE.langManager.formatMessage("commands.slash.description"))
                .addOption(ApplicationCommandOptionData.builder()
                        .name(COMMAND_OPTION)
                        .description(Mc2Discord.INSTANCE.langManager.formatMessage("commands.slash.option"))
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .autocomplete(true)
                        .build())
                .build();
    }

    /**
     * Execute a command sent from Discord, "help" lists the commands available to the member
     * @param command The command, without prefix
     * @return The output of the command, empty if the command did not output anything
     */
    public static Mono<String> execute(String command, Member member) {
        Permissions permissions = Permissions.of(member);
        if (command.equals("help"))
            return Mono.fromSupplier(() -> Mc2Discord.INSTANCE.minecraft.executeHelpCommand(permissions.level(), permissions.commands()));

        return Mc2Discord.INSTANCE.minecraft.executeCommand(command, permissions.levelFor(command))
                .timeout(COMMAND_TIMEOUT, Mono.fromSupplier(() -> Mc2Discord.INSTANCE.langManager.formatMessage("commands.timeout"))); // Server stopping or stuck
    }

    private static boolean canUseSlashCommand(Interaction interaction) {
        return Mc2Discord.INSTANCE.vars.isStarted && Mc2Discord.INSTANCE.config.commands.slash_command && interaction.getMember().isPresent()
                && !MessageManager.getMatchingChannels(List.of("command"), interaction.getChannelId()).isEmpty();
    }

    private static String removeSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }

    public static Mono<Void> onChatInputInteraction(ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals(SLASH_COMMAND_NAME))
            return Mono.empty();

        Interaction interaction = event.getInteraction();
        if (!canUseSlashCommand(interaction))
            return event.reply(Mc2Discord.INSTANCE.langManager.formatMessage("commands.slash.wrong_channel")).withEphemeral(true);

        String command = removeSlash(event.getOptionAsString(COMMAND_OPTION).orElse(""));
        AllowedMentions allowedMentions = Mc2Discord.INSTANCE.vars.allowedMentions.get();

        return event.deferReply().then(Mono.defer(() -> execute(command, interaction.getMember().orElseThrow()))).flatMapMany(result -> {
            List<String> messages = result.isEmpty()
                    ? List.of(Mc2Discord.INSTANCE.langManager.formatMessage("commands.slash.no_output"))
                    : M2DUtils.breakStringInMessages(M2DUtils.transformToMention(result, interaction.getChannelId()), 2000,
                            Mc2Discord.INSTANCE.config.commands.use_codeblocks);

            // The first message replaces the "thinking..." reply, the others are sent after it
            return Flux.fromIterable(messages)
                    .index()
                    .concatMap(message -> message.getT1() == 0
                            ? event.editReply(message.getT2()).withAllowedMentionsOrNull(allowedMentions)
                            : event.createFollowup(message.getT2()).withAllowedMentions(allowedMentions));
        }).then();
    }

    /**
     * Suggest completions of the command being typed, like the tab completion of the console
     */
    public static Mono<Void> onChatInputAutoComplete(ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals(SLASH_COMMAND_NAME))
            return Mono.empty();

        if (!canUseSlashCommand(event.getInteraction()))
            return event.respondWithSuggestions(List.of());

        String input = removeSlash(event.getFocusedOption().getValue().map(ApplicationCommandInteractionOptionValue::getRaw).orElse(""));
        Permissions permissions = Permissions.of(event.getInteraction().getMember().orElseThrow());

        // Discord trims the typed value, so the next argument is also suggested as if a space had been typed after the current one
        String nextArgumentInput = input.isEmpty() || input.endsWith(" ") ? null : input + " ";

        return Mono
                .zip(getCompletion(input, permissions),
                        nextArgumentInput != null ? getCompletion(nextArgumentInput, permissions) : Mono.just(IMinecraft.CommandCompletion.EMPTY))
                .map(completions -> {
                    // The typed input first to keep it when submitted, then the allowed commands as they may be above the permission level
                    Stream<ApplicationCommandOptionChoiceData> typedChoices = Stream.concat(Stream.of(input, "help"), permissions.commands().stream())
                            .filter(command -> !command.isEmpty() && command.startsWith(input))
                            .map(command -> choice(command, command));
                    Set<String> names = new HashSet<>();

                    return Stream.of(typedChoices, toChoices(completions.getT1(), input), toChoices(completions.getT2(), input))
                            .flatMap(stream -> stream)
                            .filter(choice -> choice.name().length() <= MAX_CHOICE_LENGTH && choice.value().toString().length() <= MAX_CHOICE_LENGTH)
                            .filter(choice -> names.add(choice.name()))
                            .limit(MAX_CHOICES)
                            .toList();
                })
                .flatMap(event::respondWithSuggestions);
    }

    /**
     * Like in game, the usages are shown only when nothing can be suggested. As they are not valid arguments, selecting one keeps the typed input
     */
    private static Stream<ApplicationCommandOptionChoiceData> toChoices(IMinecraft.CommandCompletion completion, String input) {
        if (completion.suggestions().isEmpty())
            return completion.usages().stream().map(usage -> choice(usage, input));

        return completion.suggestions().stream().map(suggestion -> choice(suggestion, suggestion));
    }

    /**
     * @param name The text shown in the list
     * @param value The command set when selected
     */
    private static ApplicationCommandOptionChoiceData choice(String name, String value) {
        return ApplicationCommandOptionChoiceData.builder().name(name).value(value).build();
    }

    private static Mono<IMinecraft.CommandCompletion> getCompletion(String input, Permissions permissions) {
        return Mc2Discord.INSTANCE.minecraft.getCommandCompletion(input, permissions.levelFor(input))
                .timeout(Duration.ofMillis(2500), Mono.just(IMinecraft.CommandCompletion.EMPTY)) // Discord requires an answer within 3 seconds
                .onErrorReturn(IMinecraft.CommandCompletion.EMPTY);
    }
}
