package ml.denisd3d.minecraft2discord.commands;

import net.minecraft.command.ICommandSource;
import net.minecraft.util.text.ITextComponent;

public class DiscordCommandSource implements ICommandSource {

    public static String answer = ""; // This is a hack to have only one string as result for the command. We need to clear it after each use and get the result by our self

    public DiscordCommandSource()
    {
    }

    @Override
    public void sendMessage(ITextComponent component) {
        answer += component.getString() + "\n";
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
        return true;
    }


}
