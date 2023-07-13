package fr.denisd3d.mc2discord.forge;

import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.Vars;
import fr.denisd3d.mc2discord.core.events.LifecycleEvents;
import fr.denisd3d.mc2discord.forge.commands.DiscordCommandImpl;
import fr.denisd3d.mc2discord.forge.commands.DiscordCommandSource;
import fr.denisd3d.mc2discord.forge.commands.M2DCommandImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(Mc2DiscordForge.MOD_ID)
public class Mc2DiscordForge {
    public static final String MOD_ID = "mc2discord";
    public static CommandSourceStack commandSource;

    public Mc2DiscordForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ForgeEvents.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        M2DCommandImpl.register(event.getDispatcher());
        DiscordCommandImpl.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Mc2Discord.INSTANCE = new Mc2Discord(new MinecraftImpl());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LifecycleEvents.minecraftReady = true;
        Vars.startTime = System.currentTimeMillis();
        LifecycleEvents.mcOrDiscordReady();

        commandSource = new CommandSourceStack(new DiscordCommandSource(), Vec3.ZERO, Vec2.ZERO, ServerLifecycleHooks.getCurrentServer()
                .overworld(), Integer.MAX_VALUE, "Discord", Component.literal("Discord"), ServerLifecycleHooks.getCurrentServer(), null);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        LifecycleEvents.onShutdown();
    }
}
