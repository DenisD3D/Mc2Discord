package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.rest.http.client.ClientException;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.MemberEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.storage.LinkedPlayerEntry;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AccountManager {
    public static final PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<UUID, String> expirationPolicy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(10, TimeUnit.MINUTES);
    public static final PassiveExpiringMap<UUID, String> codes = new PassiveExpiringMap<>(expirationPolicy);
    public static final Random random = new Random();


    public static void init() {
        if (!Mc2Discord.INSTANCE.config.features.account_linking)
            return;

        Mc2Discord.INSTANCE.minecraft.registerAccountCommands();

        if (!Mc2Discord.INSTANCE.config.account.guild_id.equals(M2DUtils.NIL_SNOWFLAKE)) {
            for (LinkedPlayerEntry linkedPlayerEntry : Mc2Discord.INSTANCE.linkedPlayerList.getEntries()) {
                Mc2Discord.INSTANCE.client.getMemberById(Mc2Discord.INSTANCE.config.account.guild_id, linkedPlayerEntry.getDiscordId())
                        .onErrorResume(ClientException.isStatusCode(404), throwable -> {
                            Mc2Discord.INSTANCE.linkedPlayerList.remove(linkedPlayerEntry.getPlayerUuid());
                            return Mono.empty();
                        })
                        .subscribe(member -> renameDiscordAccount(linkedPlayerEntry.getPlayerUuid(), linkedPlayerEntry.getDiscordId()));
            }
        }
    }

    public static void stop() {
        if (!Mc2Discord.INSTANCE.config.features.account_linking)
            return;

        codes.clear();
    }

    public static void renameDiscordAccount(UUID uuid, Snowflake discordId) {
        if (!Mc2Discord.INSTANCE.config.account.rename_discord_member)
            return;

        Mc2Discord.INSTANCE.client.getMemberById(Mc2Discord.INSTANCE.config.account.guild_id, discordId)
                .filterWhen(AccountManager::isBellow)
                .zipWhen(M2DUtils::getMemberColor)
                .flatMap(memberIntegerTuple2 -> {
                    Member member = memberIntegerTuple2.getT1();
                    Integer color = memberIntegerTuple2.getT2();

                    return member.edit(GuildMemberEditSpec.builder().nicknameOrNull(Entity.replace(Mc2Discord.INSTANCE.config.account.discord_pseudo_format, Arrays.asList(
                            new PlayerEntity(Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(uuid), Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(uuid), uuid),
                            new MemberEntity(member.getGlobalName().orElse(member.getUsername()), member.getUsername(), member.getNickname().orElse(""), member.getAvatarUrl(), color)))).build());
                }).subscribe();

    }

    /**
     * Check if the member is below than the bot
     *
     * @param member The member to check
     * @return Mono<Boolean> that resolve to true if the member is below than the bot, false otherwise
     */
    private static Mono<Boolean> isBellow(Member member) {
        return member.isHigher(Mc2Discord.INSTANCE.client.getSelfId()).map(isBellow -> !isBellow);
    }

    /**
     * Check if the player is linked or generate a link code
     *
     * @param player_uuid The player uuid
     * @return null if the player is linked, the link code otherwise
     */
    public static String checkLinkedOrGenerateCode(UUID player_uuid) {
        if (Mc2Discord.INSTANCE.linkedPlayerList.contains(player_uuid)) {
            return null; // The player is linked
        }

        String code;
        if (codes.containsKey(player_uuid)) {
            code = codes.get(player_uuid); // The player has already a code
        } else {
            do { // Generate a new code
                code = String.format("%04d", random.nextInt(10000));
            } while (codes.containsValue(code));
            codes.put(player_uuid, code);
        }

        return code;
    }

    public static boolean onMessageCreate(MessageCreateEvent event) {
        if (!Mc2Discord.INSTANCE.config.features.account_linking)
            return false; // The account linking is disabled

        if (event.getGuildId().isPresent())
            return false; // The message is not in private message

        String message = event.getMessage().getContent();

        if (!message.startsWith("!code"))
            return false; // The message is not a code

        String code = message.substring(5).trim();

        UUID player_uuid = codes.entrySet().stream().filter(entry -> entry.getValue().equals(code)).map(PassiveExpiringMap.Entry::getKey).findFirst().orElse(null);

        if (player_uuid == null) {
            event.getMessage().getRestChannel().createMessage(Mc2Discord.INSTANCE.config.account.messages.link_invalid_code.asString()).subscribe();
        } else {
            codes.remove(player_uuid);

            if (!Mc2Discord.INSTANCE.config.account.guild_id.equals(M2DUtils.NIL_SNOWFLAKE)) {
                event.getMessage().getAuthor().orElseThrow(() -> new RuntimeException("Message author is null"))
                        .asMember(Mc2Discord.INSTANCE.config.account.guild_id)
                        .flatMapMany(member -> Mono.fromSupplier(() -> Mc2Discord.INSTANCE.config.account.policies)
                                .flatMapMany(Flux::fromIterable)
                                .filter(policy -> policy.required_roles_id.size() == 0 || (member.getRoleIds()
                                        .size() != 0 && member.getRoleIds().containsAll(policy.required_roles_id)))
                                .switchIfEmpty(event.getMessage()
                                        .getRestChannel()
                                        .createMessage(Mc2Discord.INSTANCE.config.account.messages.missing_roles.asString())
                                        .then(Mono.empty()))
                                .doOnNext(accountPolicy -> validate_link(player_uuid, event))
                                .map(policy -> policy.roles_id_to_give)
                                .flatMap(Flux::fromIterable)
                                .flatMap(member::addRole))
                        .subscribe();
            } else {
                validate_link(player_uuid, event);
            }
        }

        return true;
    }

    private static void validate_link(UUID uuid, MessageCreateEvent event) {
        Mc2Discord.LOGGER.info("New account linked (Discord: " + event.getMessage()
                .getAuthor()
                .map(User::getId)
                .map(Snowflake::asLong)
                .orElse(0L) + ", MC: " + uuid + ")");


        Mc2Discord.INSTANCE.linkedPlayerList.add(new LinkedPlayerEntry(uuid, event.getMessage().getAuthor().get().getId()));

        event.getMessage().getRestChannel().createMessage(Mc2Discord.INSTANCE.config.account.messages.link_successful.asString()).subscribe();
        codes.remove(uuid);

        if (!Mc2Discord.INSTANCE.config.account.guild_id.equals(M2DUtils.NIL_SNOWFLAKE)) {
            renameDiscordAccount(uuid, event.getMessage().getAuthor().get().getId());
        }
    }

    public static boolean unlinkAccount(UUID uuid) {
        if (Mc2Discord.INSTANCE.linkedPlayerList.contains(uuid)) {
            Mc2Discord.INSTANCE.linkedPlayerList.remove(uuid);
            return true;
        }
        return false;
    }
}
