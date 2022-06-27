package ml.denisd3d.mc2discord.forge.account;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.UUID;

public class UnknowableGameProfileArgument extends GameProfileArgument {
    public static final SimpleCommandExceptionType INVALID_PLAYER = new SimpleCommandExceptionType(new StringTextComponent("Invalid player"));

    public static UnknowableGameProfileArgument unknowableGameProfileArgument() {
        return new UnknowableGameProfileArgument();
    }

    @Override
    public IProfileProvider parse(StringReader p_parse_1_) throws CommandSyntaxException {
        if (p_parse_1_.canRead() && p_parse_1_.peek() == '@') {
            EntitySelectorParser entityselectorparser = new EntitySelectorParser(p_parse_1_);
            EntitySelector entityselector = entityselectorparser.parse();
            if (entityselector.includesEntities()) {
                throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create();
            } else {
                return new GameProfileArgument.ProfileProvider(entityselector);
            }
        } else {
            int i = p_parse_1_.getCursor();

            while (p_parse_1_.canRead() && p_parse_1_.peek() != ' ') {
                p_parse_1_.skip();
            }

            String s = p_parse_1_.getString().substring(i, p_parse_1_.getCursor());
            return (p_197107_1_) -> {
                GameProfile gameprofile = p_197107_1_.getServer().getProfileCache().get(s);
                if (gameprofile == null) {
                    try {
                        return Collections.singleton(new GameProfile(UUID.fromString(s), null));
                    } catch (IllegalArgumentException illegalargumentexception) {
                        throw INVALID_PLAYER.create();
                    }
                } else {
                    return Collections.singleton(gameprofile);
                }
            };
        }
    }
}
