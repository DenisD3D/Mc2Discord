package ml.denisd3d.mc2discord.forge.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.management.module.ItemSharingModule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(value = ItemSharingModule.class, remap = false)
public class ItemSharingModuleMixin {

    @Inject(method = "linkItem", at = @At(value = "RETURN"))
    private static void linkItem(PlayerEntity player, ItemStack item, CallbackInfo ci) {
        if (!ModuleLoader.INSTANCE.isModuleEnabled(ItemSharingModule.class)) return;

        if (!item.isEmpty() && player instanceof ServerPlayerEntity) {
            ITextComponent component = item.getDisplayName();

            HoverEvent hoverEvent = component.getSiblings().get(0).getStyle().getHoverEvent();
            if (hoverEvent == null) return;

            HoverEvent.ItemHover value = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (value == null) return;

            ItemStack itemStack = new ItemStack(value.item, value.count);
            if (value.tag != null) {
                itemStack.setTag(value.tag);
            }

            EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
            builder.color(Color.of(88, 101, 242));
            fillEmbed(itemStack, builder);

            Mc2Discord.INSTANCE.messageManager.sendInfoMessage("", builder.build());
        }
    }

    private static void fillEmbed(ItemStack itemStack, EmbedCreateSpec.Builder builder) {
        // Same method as getTooltipLines but adapted
        StringBuilder description = new StringBuilder();
        IFormattableTextComponent iFormattableTextComponent = (new StringTextComponent("")).append(itemStack.getHoverName())
                .withStyle(itemStack.getRarity().color);
        if (itemStack.hasCustomHoverName()) {
            iFormattableTextComponent.withStyle(TextFormatting.ITALIC);
        }
        builder.title("[" + iFormattableTextComponent.getString() + (itemStack.getCount() > 1 ? " (x" + itemStack.getCount() + ")" : "") + "]");

        if (itemStack.hasTag()) {
            List<ITextComponent> enchantments = Lists.newArrayList();
            for(int i = 0; i < itemStack.getEnchantmentTags().size(); ++i) {
                CompoundNBT compoundnbt = itemStack.getEnchantmentTags().getCompound(i);
                Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compoundnbt.getString("id"))).ifPresent((p_222123_2_) -> {
                    enchantments.add(p_222123_2_.getFullname(compoundnbt.getInt("lvl")));
                });
            }

            if (!enchantments.isEmpty()) {
                description.append("Enchantments:\n")
                        .append(enchantments.stream().map(ITextComponent::getString).collect(Collectors.joining("\n")))
                        .append("\n");
            }

            if (itemStack.getTag() != null && itemStack.getTag().contains("display", 10)) {
                CompoundNBT compoundtag = itemStack.getTag().getCompound("display");
                if (compoundtag.contains("color", 99)) {
                    description.append(new TranslationTextComponent("item.dyed").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC))
                            .append("\n\n");
                }

                if (compoundtag.getTagType("Lore") == 9) {
                    ListNBT listtag = compoundtag.getList("Lore", 8);
                    description.append("Lore:\n");
                    for (int i = 0; i < listtag.size(); ++i) {
                        String s = listtag.getString(i);

                        try {
                            IFormattableTextComponent mutablecomponent1 = ITextComponent.Serializer.fromJson(s);
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
        for (EquipmentSlotType equipmentslot : EquipmentSlotType.values()) {
            Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(equipmentslot);
            if (!multimap.isEmpty()) {
                equipment.append("\n")
                        .append(new TranslationTextComponent("item.modifiers." + equipmentslot.getName()).withStyle(TextFormatting.GRAY).getString())
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
                        equipment.append(new TranslationTextComponent("attribute.modifier.plus." + attributemodifier.getOperation()
                                .toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(entry.getKey()
                                .getDescriptionId()))
                                .withStyle(TextFormatting.BLUE)
                                .getString()).append("\n");
                    } else if (d0 < 0.0D) {
                        d1 *= -1.0D;
                        equipment.append(new TranslationTextComponent("attribute.modifier.take." + attributemodifier.getOperation()
                                .toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(entry.getKey()
                                .getDescriptionId()))
                                .withStyle(TextFormatting.RED)
                                .getString()).append("\n");
                    }
                }
            }
        }

        if (equipment.length() > 0) {
            description.append(equipment.deleteCharAt(equipment.length() - 1)).append("\n");
        }

        if (itemStack.hasTag() && itemStack.getTag() != null) {
            if (itemStack.getTag().getBoolean("Unbreakable")) {
                description.append(new TranslationTextComponent("item.unbreakable").withStyle(TextFormatting.BLUE).getString()).append("\n");
            }

            if (itemStack.getTag().contains("CanDestroy", 9)) {
                ListNBT listtag1 = itemStack.getTag().getList("CanDestroy", 8);
                if (!listtag1.isEmpty()) {
                    description.append(new TranslationTextComponent("item.canBreak").withStyle(TextFormatting.GRAY).getString()).append("\n");
                    for (int k = 0; k < listtag1.size(); ++k) {
                        expandBlockState(listtag1.getString(k))
                                .forEach(component -> description.append(component.getString()).append("\n"));
                    }
                    description.append("\n");
                }
            }

            if (itemStack.getTag().contains("CanPlaceOn", 9)) {
                ListNBT listtag2 = itemStack.getTag().getList("CanPlaceOn", 8);
                if (!listtag2.isEmpty()) {
                    description.append(new TranslationTextComponent("item.canPlace").withStyle(TextFormatting.GRAY).getString()).append("\n");

                    for (int l = 0; l < listtag2.size(); ++l) {
                        expandBlockState(listtag2.getString(l))
                                .forEach(component -> description.append(component.getString()).append("\n"));
                    }
                    description.append("\n");
                }
            }
        }

        builder.description(description.toString());
    }

    private static Collection<ITextComponent> expandBlockState(String p_206845_0_) {
        try {
            BlockStateParser blockstateparser = (new BlockStateParser(new StringReader(p_206845_0_), true)).parse(true);
            BlockState blockstate = blockstateparser.getState();
            ResourceLocation resourcelocation = blockstateparser.getTag();
            boolean flag = blockstate != null;
            boolean flag1 = resourcelocation != null;
            if (flag || flag1) {
                if (flag) {
                    return Lists.newArrayList(new TranslationTextComponent(blockstate.getBlock().getDescriptionId()).withStyle(TextFormatting.DARK_GRAY));
                }

                ITag<Block> itag = BlockTags.getAllTags().getTag(resourcelocation);
                if (itag != null) {
                    Collection<Block> collection = itag.getValues();
                    if (!collection.isEmpty()) {
                        return collection.stream().map(block -> new TranslationTextComponent(block.getDescriptionId())).map((p_222119_0_) -> p_222119_0_.withStyle(TextFormatting.DARK_GRAY)).collect(Collectors.toList());
                    }
                }
            }
        } catch (CommandSyntaxException commandsyntaxexception) {
        }

        return Lists.newArrayList((new StringTextComponent("missingno")).withStyle(TextFormatting.DARK_GRAY));
    }
}
