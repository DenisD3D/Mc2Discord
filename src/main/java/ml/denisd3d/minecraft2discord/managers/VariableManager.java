package ml.denisd3d.minecraft2discord.managers;

import ml.denisd3d.minecraft2discord.variables.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableManager
{
    public static GlobalParameterType globalVariables = new GlobalParameterType();
    public static PlayerParameterType playerVariables = new PlayerParameterType();
    public static DeathParameterType deathVariables = new DeathParameterType();
    public static AdvancementParameterType advancementVariables = new AdvancementParameterType();
    public static MessageParameterType messageVariables = new MessageParameterType();
    public static DiscordUserType discordUserVariables = new DiscordUserType();


    public static String replace(String message)
    {
        return replace(message, new HashMap<>());
    }

    public static String replace(String message, Map<IParameterType<?>, ?> parameters)
    {
        return substituteVariables(message, parameters);
    }

    public static String substituteVariables(String template, Map<IParameterType<?>, ?> variablesLists)
    {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)}");
        Matcher matcher = pattern.matcher(template);
        // StringBuilder cannot be used here because Matcher expects StringBuffer
        StringBuffer buffer = new StringBuffer();
        while (matcher.find())
        {
            for (Map.Entry<IParameterType<?>, ?> set : variablesLists.entrySet())
            {
                if (set.getKey().containsKey(matcher.group(1)))
                {
                    String replacement = set.getKey().get(matcher.group(1), set.getValue());
                    // quote to work properly with $ and {,} signs
                    matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "");
                }
            }
            if (globalVariables.containsKey(matcher.group(1)))
            {
                String replacement = globalVariables.get(matcher.group(1), null);
                matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "");
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
