package ml.denisd3d.mc2discord.forge.commands;

import mcp.MethodsReturnNonnullByDefault;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.config.core.Channels;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DiscordCommandSender implements ICommandSender {
    public static String answer = ""; // This is a hack to have only one string as result for the command. We need to clear it after each use and get the result by our self
    public static long messageChannelId;
    public static Channels.SendMode mode;
    public static int permissionLevel = 0;
    private final MinecraftServer server;
    private long time;
    private Thread messageScheduler;

    public DiscordCommandSender(MinecraftServer serverIn) {
        this.server = serverIn;
    }

    @Override
    @MethodsReturnNonnullByDefault
    public String getName() {
        return "Discord";
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        return permissionLevel >= permLevel;
    }

    @Override
    public World getEntityWorld() {
        return this.server.getEntityWorld();
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.server;
    }

    @Override
    public void sendMessage(ITextComponent component) {
        answer += component.getUnformattedText() + ((component.getStyle().getClickEvent() != null && component.getStyle()
                .getClickEvent()
                .getAction() == ClickEvent.Action.OPEN_URL) ? " <" + component.getStyle().getClickEvent().getValue() + ">" : "") + "\n";
        scheduleMessage();
    }

    @SuppressWarnings("BusyWait")
    private void scheduleMessage() {
        time = System.currentTimeMillis();
        if (messageScheduler == null || !messageScheduler.isAlive()) {
            messageScheduler = new Thread(() -> {
                while (true) {
                    if (System.currentTimeMillis() - time > 50) {
                        Mc2Discord.INSTANCE.messageManager.sendMessageInChannel(messageChannelId, "log", answer, mode, Mc2Discord.INSTANCE.config.commands.use_codeblocks, null);

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
