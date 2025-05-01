package fr.denisd3d.mc2discord.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.Vars;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.LifecycleEvents;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandImpl;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandSource;
import fr.denisd3d.mc2discord.minecraft.commands.M2DCommandImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class Mc2DiscordMinecraft {
    public static CommandSourceStack commandSource;
    public static MinecraftServer server;

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext buildContext) {
        M2DCommandImpl.register(commandSourceStackCommandDispatcher, buildContext);
        DiscordCommandImpl.register(commandSourceStackCommandDispatcher);
    }

    public static void onServerStarting(MinecraftServer minecraftServer) {
        server = minecraftServer;
        Vars.startTime = System.currentTimeMillis();
        Mc2Discord.INSTANCE = new Mc2Discord(new MinecraftImpl());
    }

    public static void onServerStarted(MinecraftServer minecraftServer) {
        LifecycleEvents.minecraftReady = true;
        commandSource = new CommandSourceStack(new DiscordCommandSource(), Vec3.ZERO, Vec2.ZERO, minecraftServer.overworld(), Integer.MAX_VALUE, "Discord", Component.literal("Discord"), minecraftServer, null);
        LifecycleEvents.mcOrDiscordReady();
    }


    public static void onServerStopped(MinecraftServer ignoredMinecraftServer) {
        LifecycleEvents.onShutdown();
        server = null;
    }
}
