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
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class WriteModuleTrigger extends AbstractCriterionTrigger<WriteModuleTrigger.Instance> {
	private static final ResourceLocation ID = new ResourceLocation(ModMain.MOD_ID, "write_module");

	public ResourceLocation getId() { return ID; }

	public Instance createInstance(JsonObject json, AndPredicate predicate, ConditionArrayParser parser) {
		return new Instance(predicate, WriteModuleType.fromString(JSONUtils.getAsString(json, "type")));
	}

	public void trigger(ServerPlayerEntity player, WriteModuleType type) {
		this.trigger(player, instance -> instance.matches(type));
	}

	public static class Instance extends CriterionInstance {
		private final WriteModuleType type;

		public Instance(AndPredicate predicate, WriteModuleType type) {
			super(ID, predicate);
			this.type = type;
		}

		public JsonObject serializeToJson(ConditionArraySerializer serializer) {
			JsonObject json = super.serializeToJson(serializer);
			if (this.type != null) json.addProperty("exterior", this.type.toString());
			return json;
		}

		public boolean matches(WriteModuleType type) {
			return this.type.equals(type);
		}
	}
	
	public static enum WriteModuleType {
		WAYPOINT("waypoint"),
		STRUCTURE("structure"),
		BIOME("biome");
		
		private String string;
		private WriteModuleType(String string) { this.string = string; }
		public String toString() { return string; }
		
	    public static WriteModuleType fromString(String string) {
	        for (WriteModuleType enumValue : WriteModuleType.values()) {
	            if (enumValue.toString().equalsIgnoreCase(string)) return enumValue;
	        }
	        return null;
	    }
	}
}
