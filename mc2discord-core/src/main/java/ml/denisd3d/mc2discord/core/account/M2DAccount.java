package ml.denisd3d.mc2discord.core.account;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.rest.http.client.ClientException;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.entities.Entity;
import ml.denisd3d.mc2discord.core.entities.Member;
import ml.denisd3d.mc2discord.core.entities.Player;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class M2DAccount {
    public final PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<UUID, String> expirationPolicy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(10, TimeUnit.MINUTES);

    public final PassiveExpiringMap<UUID, String> codes = new PassiveExpiringMap<>(expirationPolicy);

    public final Random random = new Random();
    public final IAccount iAccount;

    public M2DAccount(@Nonnull IAccount iAccount) {
        this.iAccount = iAccount;
        iAccount.loadDiscordIds();
        iAccount.saveDiscordIds();
    }


    /**
     * @param gameProfile      gameProfile to check
     * @param gameProfile_uuid gameProfile to check Uuid
     * @return the generated code or null if this player account is already linked
     */
    public String generateCodeOrNull(Object gameProfile, UUID gameProfile_uuid) {
        if (System.getProperties().getProperty("Mc2DiscordDev") != null) {
            System.out.println("Mc2Discord dev mode detected, reloading account list");
            iAccount.loadDiscordIds();
            iAccount.saveDiscordIds();
        }

        if (iAccount.contains(gameProfile)) {
            return null;
        } else {
            String code;
            if (codes.containsKey(gameProfile_uuid)) {
                code = codes.get(gameProfile_uuid);
            } else {
                do {
                    code = String.format("%04d", random.nextInt(10000));
                } while (codes.containsValue(code));
                codes.put(gameProfile_uuid, code);
            }

            return Entity.replace(Mc2Discord.INSTANCE.config.account.messages.link_get_code, Collections.emptyList())
                    .replace("${code}", code);
        }
    }

    public boolean onMessage(MessageCreateEvent event) {
        if (!event.getGuildId().isPresent()) { // It's a private message
            String message = event.getMessage().getContent();
            if (message.startsWith("!code ")) {
                if (codes.containsValue(message.substring("!code ".length()))) {
                    UUID uuid = codes.entrySet()
                            .stream()
                            .filter(uuidStringEntry -> uuidStringEntry.getValue()
                                    .equals(message.substring("!code ".length())))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);
                    if (uuid != null && event.getMessage().getAuthor().isPresent()) {
                        if (Mc2Discord.INSTANCE.config.account.guild_id != 0) {
                            event.getMessage()
                                    .getAuthor()
                                    .get()
                                    .asMember(Snowflake.of(Mc2Discord.INSTANCE.config.account.guild_id))
                                    .flatMapMany(member -> Mono.fromSupplier(() -> Mc2Discord.INSTANCE.config.account.policies)
                                            .flatMapMany(Flux::fromIterable)
                                            .filter(policy -> policy.required_roles_id.size() == 0 || (member.getRoleIds()
                                                    .size() != 0 && member.getRoleIds()
                                                    .containsAll(policy.required_roles_id)))
                                            .switchIfEmpty(event.getMessage()
                                                    .getRestChannel()
                                                    .createMessage(Mc2Discord.INSTANCE.config.account.messages.missing_roles)
                                                    .then(Mono.empty()))
                                            .doOnNext(accountPolicy -> validate_link(uuid, event))
                                            .map(policy -> policy.roles_id_to_give)
                                            .flatMap(Flux::fromIterable)
                                            .flatMap(member::addRole))
                                    .subscribe();
                        } else {
                            validate_link(uuid, event);
                        }
                    } else {
                        event.getMessage()
                                .getRestChannel()
                                .createMessage("An unexpected error occurred! Please try again")
                                .subscribe();
                    }
                } else {
                    event.getMessage()
                            .getRestChannel()
                            .createMessage(Mc2Discord.INSTANCE.config.account.messages.link_invalid_code)
                            .subscribe();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void validate_link(UUID uuid, MessageCreateEvent event) {
        Mc2Discord.logger.info("New account linked (Discord: " + event.getMessage()
                .getAuthor()
                .map(User::getId)
                .map(Snowflake::asLong)
                .orElse(0L) + ", MC: " + uuid + ")");

        if (!iAccount.add(uuid, event.getMessage().getAuthor().get().getId().asLong())) {
            event.getMessage()
                    .getRestChannel()
                    .createMessage("Failed to link account")
                    .subscribe();
            Mc2Discord.logger.error("Failed to link account");
            return;
        }

        event.getMessage()
                .getRestChannel()
                .createMessage(Mc2Discord.INSTANCE.config.account.messages.link_successful)
                .subscribe();
        codes.remove(uuid);

        iAccount.sendLinkSuccess(uuid);
        if (Mc2Discord.INSTANCE.config.account.guild_id != 0) {
            renameDiscordAccount(uuid, event.getMessage().getAuthor().get().getId());
        }
    }

    public void renameDiscordAccount(UUID uuid, Snowflake authorId) {
        renameDiscordAccount(uuid, iAccount.getInGameName(uuid), authorId);
    }

    public void renameDiscordAccountL(UUID uuid, String name, long authorId) {
        renameDiscordAccount(uuid, name, Snowflake.of(authorId));
    }

    public void renameDiscordAccount(UUID uuid, String name, Snowflake authorId) {
        if (!Mc2Discord.INSTANCE.config.account.rename_discord_member) return;
        Mc2Discord.INSTANCE.client.getMemberById(Snowflake.of(Mc2Discord.INSTANCE.config.account.guild_id), authorId)
                .subscribe(member -> Mc2Discord.INSTANCE.client.getSelfMember(Snowflake.of(Mc2Discord.INSTANCE.config.account.guild_id))
                        .flatMap(member::isHigher)
                        .flatMap(isHigher -> {
                            if (!isHigher) {
                                return member.edit(GuildMemberEditSpec.builder()
                                        .nicknameOrNull(Entity.replace(Mc2Discord.INSTANCE.config.account.discord_pseudo_format, Arrays.asList(new Player(name, name, uuid), new Member(member.getUsername(), member.getDiscriminator(), member.getNickname()
                                                .orElse(""), member.getAvatarUrl()))))
                                        .build());
                            } else {
                                Mc2Discord.logger.warn("Can't change pseudo for member " + member.getDisplayName() + " (IG : " + name + ")");
                                return Mono.empty();
                            }
                        })
                        .subscribe());
    }

    public void onStart() {
        if (!Mc2Discord.INSTANCE.config.features.account_linking) {
            return;
        }

        iAccount.updateCommands();

        iAccount.checkAllDiscordAccount();
    }

    public void checkDiscordAccount(UUID id, String name, long discordId) {
        Mc2Discord.INSTANCE.client.getMemberById(Snowflake.of(Mc2Discord.INSTANCE.config.account.guild_id), Snowflake.of(discordId))
                .onErrorResume(ClientException.isStatusCode(404), throwable -> {
                    iAccount.remove(id);
                    return Mono.empty();
                })
                .subscribe(member -> this.renameDiscordAccountL(id, name, discordId));
    }

    public void onMemberLeave(MemberLeaveEvent memberLeaveEvent) {
        iAccount.removeIfPresent(memberLeaveEvent.getUser());
    }
}