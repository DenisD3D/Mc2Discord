package ml.denisd3d.minecraft2discord;

import ml.denisd3d.minecraft2discord.api.M2DExtension;

import java.util.function.Function;

public class ExtensionUtils
{

    public static boolean executeExtensions(Function<M2DExtension, Boolean> func)
    {
        boolean r = false;
        for (M2DExtension e : Minecraft2Discord.extensions)
        {
            Boolean result = func.apply(e);
            if (result == null)
            {
                r = true;
                break;
            } else if (!result)
            {
                r = true;
            }
        }
        return r;
    }
}
