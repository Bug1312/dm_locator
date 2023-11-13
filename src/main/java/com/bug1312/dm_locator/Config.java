// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final ServerConfig SERVER_CONFIG;

	static {
		Pair<ServerConfig, ForgeConfigSpec> serverConfigPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_CONFIG = serverConfigPair.getLeft();
		SERVER_SPEC = serverConfigPair.getRight();
	}

	public static class ServerConfig {
		public final ConfigValue<Boolean> fastFeature;
		private final ConfigValue<List<? extends String>> structureBlacklist;
		private final ConfigValue<List<? extends String>> biomeBlackList;
		
		public List<ResourceLocation> getStructureBlacklist() {
			return structureBlacklist.get().stream().map(s -> new ResourceLocation(s)).collect(Collectors.toList());
		}
		
		public List<ResourceLocation> getBiomeBlacklist() {
			return biomeBlackList.get().stream().map(s -> new ResourceLocation(s)).collect(Collectors.toList());
		}

		
		ServerConfig(Builder builder) {
			builder.comment("Structure Locator DM Addon Configuration Settings").push("server");
			
			this.fastFeature = builder.comment(new String[] {
					"Sacrifice accuracy for speed when writing structures to data modules",
					"When enabled the game will check the bounding box of the entire structure instead of its individual pieces to see if you are inside of it"
			}).define("fast_write_math", false);
			
			this.structureBlacklist = builder.comment(new String[] {
					"Disallows writing and locating certain structures",
					"Example: [\"minecraft:endcity\", \"minecraft:buried_treasure\"]"
			}).defineList("structure_blacklist", Collections.emptyList(), s -> ResourceLocation.isValidResourceLocation((String) s));

			this.biomeBlackList = builder.comment(new String[] {
					"Disallows writing and locating certain biomes",
					"Example: [\"minecraft:plains\", \"minecraft:buried_treasure\"]"
			}).defineList("biome_blacklist", Collections.emptyList(), s -> ResourceLocation.isValidResourceLocation((String) s));
		}
	}
}
