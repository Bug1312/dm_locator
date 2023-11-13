// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;

public class StructureHelper {

	public static final Map<UUID, CompoundNBT> FLYING_PLAYERS_LOCATOR = new HashMap<>();
	public static boolean isFlyingWithLocator = false;

	private static final Predicate<Pair<MutableBoundingBox, Vector3i>> IS_INSIDE = (pair) -> {
		MutableBoundingBox bb = pair.getFirst();
		Vector3i pos = pair.getSecond();
		// Certain structures don't have/require an air gap, 
		// if player is directly above or next to the structure, I still want to pass
		return (
			pos.getX() >= bb.x0-1 && pos.getX() <= bb.x1+1 &&
			pos.getZ() >= bb.z0-1 && pos.getZ() <= bb.z1+1 &&
			pos.getY() >= bb.y0-1 && pos.getY() <= bb.y1+1
		);
	};

	public static Optional<ResourceLocation> getStructure(ServerPlayerEntity player) {
		BlockPos pos = player.blockPosition();
		
		StructureManager featureManager = player.getLevel().structureFeatureManager();
		return player.getLevel().getChunk(player.blockPosition()).getAllReferences().entrySet().stream() 
			// Only look at structures with references inside chunk
			.filter(set -> set.getValue().size() > 0) 
			.filter(set -> 
				DataFixUtils.orElse(featureManager.startsForFeature(SectionPos.of(pos), set.getKey())
					// Only if player is inside entire structure
					.filter((start) -> IS_INSIDE.test(Pair.of(start.getBoundingBox(), pos))) 
					// Skip if fastFeature enabled
					.filter((start) -> Config.SERVER_CONFIG.fastFeature.get() || start.getPieces().stream() 
						// Only if player is inside structures' piece
						.anyMatch((piece) -> IS_INSIDE.test(Pair.of(piece.getBoundingBox(), pos)))) 
					.findFirst(), StructureStart.INVALID_START).isValid())
			.findFirst().map(entry -> entry.getKey().getRegistryName());
	}
	
	public static String formatResourceLocationName(ResourceLocation resourceLocation) {
		String reformatted = resourceLocation.getPath().replaceAll("_", " ");
		Pattern pattern = Pattern.compile("(^[a-z]| [a-z])");
		Matcher matcher = pattern.matcher(reformatted);
		StringBuffer noKeyName = new StringBuffer();

		while (matcher.find()) matcher.appendReplacement(noKeyName, matcher.group().toUpperCase());
		
		matcher.appendTail(noKeyName);
		
		return noKeyName.toString();
	}
	
}
