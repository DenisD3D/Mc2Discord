package fr.denisd3d.mc2discord.minecraft;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.5 {
import java.net.URI;
//?}

//? if >=1.21.11 {
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
//?}

//? if >=26.2 {
import net.minecraft.network.chat.ResolutionContext;
//?}

/**
 * Minecraft code that is not the same on every supported version.
 * The variants are selected by Stonecutter comments (https://stonecutter.kikugie.dev/).
 */
public class VersionCompat {

    // Chat components

    public static ClickEvent openUrl(String url) {
        //? if >=1.21.5 {
        return new ClickEvent.OpenUrl(URI.create(url));
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        *///?}
    }

    public static ClickEvent suggestCommand(String command) {
        //? if >=1.21.5 {
        return new ClickEvent.SuggestCommand(command);
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
        *///?}
    }

    public static ClickEvent copyToClipboard(String value) {
        //? if >=1.21.5 {
        return new ClickEvent.CopyToClipboard(value);
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value);
        *///?}
    }

    public static HoverEvent showText(Component text) {
        //? if >=1.21.5 {
        return new HoverEvent.ShowText(text);
        //?} else {
        /*return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        *///?}
    }

    /**
     * @return the url opened by the click event or null if it is not an open url click event
     */
    public static String getOpenedUrl(ClickEvent clickEvent) {
        //? if >=1.21.5 {
        return clickEvent instanceof ClickEvent.OpenUrl openUrl ? openUrl.uri().toString() : null;
        //?} else {
        /*return clickEvent != null && clickEvent.getAction() == ClickEvent.Action.OPEN_URL ? clickEvent.getValue() : null;
        *///?}
    }

    /**
     * @return the component argument of a command with its selectors, scores... resolved
     */
    public static Component resolveComponentArgument(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        //? if >=26.2 {
        return ComponentUtils.resolve(ResolutionContext.create(context.getSource()), ComponentArgument.getResolvedComponent(context, name), 0);
        //?} elif >=1.21.11 {
        /*return ComponentUtils.updateForEntity(context.getSource(), ComponentArgument.getResolvedComponent(context, name), null, 0);
        *///?} else {
        /*return ComponentUtils.updateForEntity(context.getSource(), ComponentArgument.getComponent(context, name), null, 0);
        *///?}
    }

    // Permissions

    /**
     * @return a command source with every permission and named Discord
     */
    public static CommandSourceStack createCommandSource(CommandSource source, MinecraftServer server) {
        //? if >=26.3 {
        return new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, server.overworld(), PermissionSet.ALL_PERMISSIONS, Component.literal("Discord"), server);
        //?} elif >=1.21.11 {
        /*return new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, server.overworld(), PermissionSet.ALL_PERMISSIONS, "Discord", Component.literal("Discord"),
                server, null);
        *///?} else {
        /*return new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, server.overworld(), Integer.MAX_VALUE, "Discord", Component.literal("Discord"), server,
                null);
        *///?}
    }

    public static CommandSourceStack withPermissionLevel(CommandSourceStack source, int permissionLevel) {
        //? if >=1.21.11 {
        return source.withPermission(fromPermissionLevel(permissionLevel));
        //?} else {
        /*return source.withPermission(permissionLevel);
        *///?}
    }

    public static boolean isGameMaster(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        //?} else {
        /*return source.hasPermission(2);
        *///?}
    }

    //? if >=1.21.11 {
    private static PermissionSet fromPermissionLevel(int levelId) {
        if (levelId <= 0) {
            return PermissionSet.NO_PERMISSIONS;
        }
        // Same permissions as a player of this level, including the entity selectors
        return LevelBasedPermissionSet.forLevel(PermissionLevel.byId(levelId));
    }
    //?}
}
