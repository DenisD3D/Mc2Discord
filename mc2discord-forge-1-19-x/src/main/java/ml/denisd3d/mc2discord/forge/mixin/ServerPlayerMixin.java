package ml.denisd3d.mc2discord.forge.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"))
    public void sendSystemMessage(Component component, boolean p_240545_, CallbackInfo ci) {
        if (component.getContents() instanceof TranslatableContents && ((TranslatableContents) component.getContents()).getKey()
                .equals("quark.misc.shared_item") && component.getSiblings().size() >= 1) { // It's a quark message with a shared item
            HoverEvent hoverEvent = component.getSiblings().get(0).getStyle().getHoverEvent();
            if (hoverEvent == null) {
                return;
            }

            HoverEvent.ItemStackInfo value = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (value == null) {
                return;
            }

            ItemStack itemStack = value.getItemStack();

            EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
            builder.color(Color.of(88, 101, 242));
            fillEmbed(itemStack, builder);

            Mc2Discord.INSTANCE.messageManager.sendInfoMessage(component.getString(), builder.build());
        }
    }

    public void fillEmbed(ItemStack itemStack, EmbedCreateSpec.Builder builder) {
        // Same method as getTooltipFromItem but adapted
        TooltipFlag tooltipFlag = TooltipFlag.Default.NORMAL;
        StringBuilder description = new StringBuilder();
        MutableComponent mutablecomponent = Component.empty().append(itemStack.getHoverName()).withStyle(itemStack.getRarity().getStyleModifier());
        if (itemStack.hasCustomHoverName()) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }
        builder.title("[" + mutablecomponent.getString() + (itemStack.getCount() > 1 ? " (x" + itemStack.getCount() + ")" : "") + "]");

        if (itemStack.hasTag()) {
            List<Component> enchantments = Lists.newArrayList();
            ItemStack.appendEnchantmentNames(enchantments, itemStack.getEnchantmentTags());

            if (!enchantments.isEmpty()) {
                description.append("Enchantments:\n")
                        .append(enchantments.stream().map(Component::getString).collect(Collectors.joining("\n")))
                        .append("\n");
            }

            if (itemStack.getTag() != null && itemStack.getTag().contains("display", 10)) {
                CompoundTag compoundtag = itemStack.getTag().getCompound("display");
                if (compoundtag.contains("color", 99)) {
                    description.append(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)).append("\n\n");
                }

                if (compoundtag.getTagType("Lore") == 9) {
                    ListTag listtag = compoundtag.getList("Lore", 8);
                    description.append("Lore:\n");
                    for (int i = 0; i < listtag.size(); ++i) {
                        String s = listtag.getString(i);

                        try {
                            MutableComponent mutablecomponent1 = Component.Serializer.fromJson(s);
                            if (mutablecomponent1 != null) {
                                description.append(mutablecomponent1.getString()).append("\n");
                            }
                        } catch (Exception exception) {
                            compoundtag.remove("Lore");
                        }
                    }
                    description.append("\n");
                }
            }
        }

        StringBuilder equipment = new StringBuilder();
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(equipmentslot);
            if (!multimap.isEmpty()) {
                equipment.append("\n")
                        .append(Component.translatable("item.modifiers." + equipmentslot.getName()).withStyle(ChatFormatting.GRAY).getString())
                        .append("\n");

                for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attributemodifier = entry.getValue();
                    double d0 = attributemodifier.getAmount();

                    double d1;
                    if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                            d1 = d0 * 10.0D;
                        } else {
                            d1 = d0;
                        }
                    } else {
                        d1 = d0 * 100.0D;
                    }

                    if (d0 > 0.0D) {
                        equipment.append(Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation()
                                        .toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().getDescriptionId()))
                                .withStyle(ChatFormatting.BLUE)
                                .getString()).append("\n");
                    } else if (d0 < 0.0D) {
                        d1 *= -1.0D;
                        equipment.append(Component.translatable("attribute.modifier.take." + attributemodifier.getOperation()
                                        .toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().getDescriptionId()))
                                .withStyle(ChatFormatting.RED)
                                .getString()).append("\n");
                    }
                }
            }
        }

        if (!equipment.isEmpty()) {
            description.append(equipment.deleteCharAt(equipment.length() - 1)).append("\n");
        }

        if (itemStack.hasTag() && itemStack.getTag() != null) {
            if (itemStack.getTag().getBoolean("Unbreakable")) {
                description.append(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE).getString()).append("\n");
            }

            if (itemStack.getTag().contains("CanDestroy", 9)) {
                ListTag listtag1 = itemStack.getTag().getList("CanDestroy", 8);
                if (!listtag1.isEmpty()) {
                    description.append(Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY).getString()).append("\n");
                    for (int k = 0; k < listtag1.size(); ++k) {
                        ItemStack.expandBlockState(listtag1.getString(k))
                                .forEach(component -> description.append(component.getString()).append("\n"));
                    }
                    description.append("\n");
                }
            }

            if (itemStack.getTag().contains("CanPlaceOn", 9)) {
                ListTag listtag2 = itemStack.getTag().getList("CanPlaceOn", 8);
                if (!listtag2.isEmpty()) {
                    description.append(Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY).getString()).append("\n");

                    for (int l = 0; l < listtag2.size(); ++l) {
                        ItemStack.expandBlockState(listtag2.getString(l))
                                .forEach(component -> description.append(component.getString()).append("\n"));
                    }
                    description.append("\n");
                }
            }
        }

        if (tooltipFlag.isAdvanced()) {
            if (itemStack.isDamaged()) {
                description.append(Component.translatable("item.durability", itemStack.getMaxDamage() - itemStack.getDamageValue(), itemStack.getMaxDamage())
                        .getString()).append("\n");
            }

            description.append(Component.literal(Registry.ITEM.getKey(itemStack.getItem()).toString())
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .getString()).append("\n");
            if (itemStack.hasTag() && itemStack.getTag() != null) {
                description.append(Component.translatable("item.nbt_tags", itemStack.getTag().getAllKeys().size())
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .getString()).append("\n");
            }
        }

        List<Component> list = Lists.newArrayList();
        net.minecraftforge.event.ForgeEventFactory.onItemTooltip(itemStack, null, list, tooltipFlag);
        description.append(list.stream().map(Component::getString).collect(Collectors.joining("\n")));
        builder.description(description.toString());
    }
}
