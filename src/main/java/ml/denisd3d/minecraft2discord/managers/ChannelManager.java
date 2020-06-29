package ml.denisd3d.minecraft2discord.managers;

import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChannelManager
{

    public static TextChannel getChatChannel()
    {
        return Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get());
    }

    public static TextChannel getInfoChannel()
    {
        return Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
    }

    public static TextChannel getTopicChannel()
    {
        return Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.topicChannel.get());
    }

    public static GuildChannel getNameChannel()
    {
        return Minecraft2Discord.getDiscordBot().getGuildChannelById(Config.SERVER.nameChannel.get());
    }
}
