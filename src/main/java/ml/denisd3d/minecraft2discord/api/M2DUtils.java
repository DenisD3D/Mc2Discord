package ml.denisd3d.minecraft2discord.api;

import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class M2DUtils
{
    public static ArrayList<ListenerAdapter> eListeners = new ArrayList<>();

    public static void registerExtension(M2DExtension mainClass)
    {
        Minecraft2Discord.extensions.add(mainClass);
    }

    public static void addEventListener(ListenerAdapter... listeners)
    {
        eListeners.addAll(Arrays.asList(listeners));
    }
}
