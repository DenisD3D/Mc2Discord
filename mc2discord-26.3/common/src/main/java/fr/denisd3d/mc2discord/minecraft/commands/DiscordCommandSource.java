package fr.denisd3d.mc2discord.minecraft.commands;

import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.util.Collections;

public class DiscordCommandSource implements CommandSource {
    public static String answer = ""; // This is a hack to have only one string as result for the command. We need to clear it after each use and get the result by our self
    public static Snowflake channelId;
    private long time;
    private Thread messageScheduler;

    @Override
    public void sendSystemMessage(Component component) {
        answer += component.getString() + ((component.getStyle().getClickEvent() != null && component.getStyle()
                .getClickEvent()
                .action() == ClickEvent.Action.OPEN_URL) ? " <" + ((ClickEvent.OpenUrl)component.getStyle()
                .getClickEvent()).uri() + ">" : "") + "\n";
        scheduleMessage();
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @SuppressWarnings("BusyWait")
    private void scheduleMessage() {
        time = System.currentTimeMillis();
        if (messageScheduler == null || !messageScheduler.isAlive()) {
            messageScheduler = new Thread(() -> {
                while (true) {
                    if (System.currentTimeMillis() - time > 50) {
                        MessageManager.sendMessage(Collections.singletonList("command"), answer, MessageManager.default_username, MessageManager.default_avatar, channelId, Mc2Discord.INSTANCE.config.commands.use_codeblocks).subscribe();

                        answer = "";
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Mc2Discord.LOGGER.error("An error occurred while waiting for the command result", e);
                    }
                }
            });
            messageScheduler.start();
        }
    }
}