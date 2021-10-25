package ml.denisd3d.mc2discord.forge.commands;

import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.UUID;

public class DiscordCommandSource implements CommandSource {
    public static String answer = ""; // This is a hack to have only one string as result for the command. We need to clear it after each use and get the result by our self
    public static long messageChannelId;
    public static boolean useWebhook;
    private long time;
    private Thread messageScheduler;

    @Override
    public void sendMessage(Component component, @Nonnull UUID p_145747_2_) {
        answer += component.getString() + ((component.getStyle().getClickEvent() != null && component.getStyle().getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) ? " <" + component.getStyle().getClickEvent().getValue() + ">" : "") + "\n";
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
                        Mc2Discord.INSTANCE.messageManager.sendMessageInChannel(messageChannelId, answer, useWebhook, Mc2Discord.INSTANCE.config.commands.use_codeblocks, null);

                        answer = "";
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Mc2Discord.logger.error(e);
                    }
                }
            });
            messageScheduler.start();
        }
    }
}
