package ml.denisd3d.minecraft2discord.forge;

import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.events.LifecycleEvents;
import ml.denisd3d.minecraft2discord.forge.commands.DiscordCommandImpl;
import ml.denisd3d.minecraft2discord.forge.commands.DiscordCommandSource;
import ml.denisd3d.minecraft2discord.forge.commands.M2DCommandImpl;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod("minecraft2discord")
@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class Minecraft2DiscordForge {
    public static final CommandSource commandSource = new CommandSource(new DiscordCommandSource(),
            Vec3d.ZERO,
            Vec2f.ZERO,
            ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD),
            4,
            "Discord",
            new StringTextComponent("Discord"),
            ServerLifecycleHooks.getCurrentServer(),
            null);

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        Minecraft2Discord.firstInit();
        Minecraft2Discord.INSTANCE = new Minecraft2Discord(false, new MinecraftImpl());
        M2DCommandImpl.register(event.getCommandDispatcher());
        DiscordCommandImpl.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        Minecraft2Discord.INSTANCE.setMinecraftStarted(true);
        LifecycleEvents.bothReadyEvent();
    }

    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event) {
        LifecycleEvents.onShutdown();
    }
}
