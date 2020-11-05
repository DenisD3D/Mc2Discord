package ml.denisd3d.minecraft2discord.api;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class M2DExtension {
    // true : continue execution
    // false : Don't execute M2D default code
    // null : Don't execute other extension and M2D code
    public void onReady(ReadyEvent event) {

    }

    public void onStop(FMLServerStoppingEvent event) {

    }

    public Boolean onDiscordMessage(MessageReceivedEvent event) {
        return true;
    }

    public Boolean onAdvancement(AdvancementEvent event) {
        return true;
    }

    public Boolean onDeath(LivingDeathEvent event) {
        return true;
    }

    public Boolean onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        return true;
    }

    public Boolean onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        return true;
    }

    public Boolean onCommand(CommandEvent event) {
        return true;
    }

    public Boolean onMinecraftMessage(ServerChatEvent event) {
        return true;
    }
}
