package ml.denisd3d.minecraft2discord.variables;

import ml.denisd3d.minecraft2discord.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AdvancementEvent;

import java.util.HashMap;
import java.util.function.Function;

public class DiscordUserType implements IParameterType<Member>
{
    private final HashMap<String, Function<Member, String>> parameters = new HashMap<>();

    public DiscordUserType()
    {
        parameters.put("discord_user_name", member -> Config.SERVER.nicknameEnabled.get() ? member.getEffectiveName() : member.getUser().getName());
        parameters.put("discord_user_tag", member -> member.getUser().getAsTag());
        parameters.put("discord_user_discriminator", member -> member.getUser().getDiscriminator());
    }

    @Override
    public String get(String key, Object variable)
    {
        return parameters.get(key).apply((Member) variable);
    }

    @Override
    public void set(String key, Function<Member, String> value)
    {
        parameters.put(key, value);
    }

    @Override
    public boolean containsKey(String key)
    {
        return parameters.containsKey(key);
    }
}
