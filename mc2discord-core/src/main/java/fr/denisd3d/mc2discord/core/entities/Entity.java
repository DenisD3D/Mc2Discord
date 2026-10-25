package fr.denisd3d.mc2discord.core.entities;

import javax.annotation.Nullable;

/**
 * Data available to templates through variables, see {@link Template#render(Entity...)}
 */
public interface Entity {
    /**
     * @return The value of the variable, or null if this entity doesn't provide it
     */
    @Nullable
    Object get(String variable);
}
