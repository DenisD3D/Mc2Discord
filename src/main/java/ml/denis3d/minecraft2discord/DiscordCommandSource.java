package ml.denis3d.minecraft2discord;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.text.ITextComponent;

public class DiscordCommandSource implements ICommandSource {

    private MessageChannel channel;

    public DiscordCommandSource(MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void sendMessage(ITextComponent component) {
        if (channel != null)
        {
            channel.sendMessage(component.getFormattedText()).submit();
        }
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldReceiveErrors() {
        return true;
    }

    @Override
    public boolean allowLogging() {
        return false;
    }
}
