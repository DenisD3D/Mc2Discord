package fr.denisd3d.mc2discord.minecraft.commands;

import fr.denisd3d.mc2discord.minecraft.VersionCompat;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Collect the output of a command executed from Discord to send it as a single message
 */
public class DiscordCommandSource implements CommandSource {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Mc2Discord command output");
        thread.setDaemon(true);
        return thread;
    });

    private final StringBuilder answer = new StringBuilder();
    private final Consumer<String> onResult;
    private ScheduledFuture<?> flushTask;
    private boolean flushed = false;

    /**
     * @param onResult Called once with the whole output of the command
     */
    public DiscordCommandSource(Consumer<String> onResult) {
        this.onResult = onResult;
    }

    @Override
    public synchronized void sendSystemMessage(Component component) {
        String url = VersionCompat.getOpenedUrl(component.getStyle().getClickEvent());
        answer.append(component.getString()).append(url != null ? " <" + url + ">" : "").append("\n");
        scheduleFlush();
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

    /**
     * Send the output once nothing has been output for 50ms
     */
    public synchronized void scheduleFlush() {
        if (flushed)
            return;

        if (flushTask != null) {
            flushTask.cancel(false);
        }
        flushTask = SCHEDULER.schedule(this::flush, 50, TimeUnit.MILLISECONDS);
    }

    private synchronized void flush() {
        if (flushed)
            return;

        flushed = true;
        onResult.accept(answer.toString());
    }
}
