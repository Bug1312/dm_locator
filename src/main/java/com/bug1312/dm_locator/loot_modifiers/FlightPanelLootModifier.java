// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.loot_modifiers;

import java.util.List;

import javax.annotation.Nonnull;

import com.bug1312.dm_locator.Register;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class FlightPanelLootModifier extends LootModifier {
	
    public FlightPanelLootModifier(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull @Override
    public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
    	generatedLoot.add(new ItemStack(Register.LOCATOR_ATTACHMENT_ITEM.get(), 1));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<FlightPanelLootModifier> {
        @Override
        public FlightPanelLootModifier read(ResourceLocation name, JsonObject json, ILootCondition[] conditionsIn) {
            return new FlightPanelLootModifier(conditionsIn);
        }

        @Override
        public JsonObject write(FlightPanelLootModifier instance) {
            return makeConditions(instance.conditions);
        }
    }

}
