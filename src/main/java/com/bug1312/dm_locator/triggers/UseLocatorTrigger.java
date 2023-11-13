// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.triggers;

import com.bug1312.dm_locator.ModMain;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class UseLocatorTrigger extends AbstractCriterionTrigger<UseLocatorTrigger.Instance> {
	private static final ResourceLocation ID = new ResourceLocation(ModMain.MOD_ID, "use_locator");

	public ResourceLocation getId() { return ID; }
	public UseLocatorTrigger.Instance createInstance(JsonObject json, AndPredicate predicate, ConditionArrayParser parser) { return new UseLocatorTrigger.Instance(predicate); }
	public void trigger(ServerPlayerEntity player) { this.trigger(player, (instance) -> true); }

	public static class Instance extends CriterionInstance {
		public Instance(AndPredicate predicate) { super(UseLocatorTrigger.ID, predicate); }
		public JsonObject serializeToJson(ConditionArraySerializer serializer) { return super.serializeToJson(serializer); }
	}
}
