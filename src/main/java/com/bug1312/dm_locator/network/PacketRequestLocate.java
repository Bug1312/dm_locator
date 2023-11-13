// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.network;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bug1312.dm_locator.Config;
import com.bug1312.dm_locator.Register;
import com.bug1312.dm_locator.StructureHelper;
import com.bug1312.dm_locator.particle.LocateParticleData;
import com.swdteam.common.init.DMFlightMode;
import com.swdteam.common.init.DMTardis;
import com.swdteam.common.init.DMTranslationKeys;
import com.swdteam.common.tardis.TardisData;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PacketRequestLocate {
	
	public PacketRequestLocate() {}
	public static void encode(PacketRequestLocate msg, PacketBuffer buf) {}
	public static PacketRequestLocate decode(PacketBuffer buf) {return new PacketRequestLocate();}

	public static void handle(PacketRequestLocate msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity player = ctx.get().getSender();
			
			if (StructureHelper.FLYING_PLAYERS_LOCATOR.containsKey(player.getUUID())) {
				Consumer<TranslationTextComponent> sendError = (text) -> player.displayClientMessage(text.setStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
				Consumer<Vector3d> toLocation = (end) -> {
					Vector3d start = player.position();
					
					Vector3d direction = end.subtract(start);	
					if (direction.length() > 15) direction = direction.normalize().scale(15);

					Vector3d limitedEnd = start.add(direction);

					Register.TRIGGER_USE.trigger(player);
					player.getLevel().sendParticles(new LocateParticleData((float) limitedEnd.x(), (float) limitedEnd.z(), (int) direction.length()), start.x, start.y, start.z, 1, 0, 0, 0, 0);						
				};
				CompoundNBT tag = StructureHelper.FLYING_PLAYERS_LOCATOR.get(player.getUUID());
				
				// Structure Data Modules
				if (tag.contains("Structure")) {
					Structure<?> structure = ForgeRegistries.STRUCTURE_FEATURES.getValue(new ResourceLocation(tag.getString("Structure")));
					if (structure != null) {
						if (!Config.SERVER_CONFIG.getStructureBlacklist().contains(structure.getRegistryName())) {
							BlockPos newPos = player.getLevel().findNearestMapFeature(structure, player.blockPosition(), 100, false);
							if (newPos != null) {
					            TardisData data = DMTardis.getTardis(DMFlightMode.getTardisID(player.getUUID()));
								// If not creative mode, at 100% fuel consumption link, use 15. At 0% use 50.
								float fuelUsage = player.abilities.instabuild ? 0 : 50 + (data.getFluidLinkFuelConsumption() / 100F) * (15 - 50);				

								if (data.getFuel() >= fuelUsage) {
									data.addFuel(-fuelUsage);
									data.save();
									toLocation.accept(new Vector3d(newPos.getX(), player.position().y(), newPos.getZ()));
								} else sendError.accept(DMTranslationKeys.TARDIS_NOT_ENOUGH_FUEL);
							} else sendError.accept(new TranslationTextComponent("commands.locate.failed"));
						} else sendError.accept(new TranslationTextComponent("commands.locate.failed"));
					} else sendError.accept(Register.TEXT_INVALID_STRUCTURE);
				// Waypoint Data Modules
				} else if (tag.contains("Biome")) {
					Optional<Biome> biome = player.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(new ResourceLocation(tag.getString("Biome")));
					if (biome.isPresent()) {
						if (!Config.SERVER_CONFIG.getStructureBlacklist().contains(biome.get().getRegistryName())) {
							BlockPos newPos = player.getLevel().findNearestBiome(biome.get(), player.blockPosition(), 6400, 8);
							if (newPos != null) {
					            TardisData data = DMTardis.getTardis(DMFlightMode.getTardisID(player.getUUID()));
								// If not creative mode, at 100% fuel consumption link, use 10. At 0% use 45.
								float fuelUsage = player.abilities.instabuild ? 0 : 45 + (data.getFluidLinkFuelConsumption() / 100F) * (10 - 45);				

								if (data.getFuel() >= fuelUsage) {
									data.addFuel(-fuelUsage);
									data.save();
									toLocation.accept(new Vector3d(newPos.getX(), player.position().y(), newPos.getZ()));
								} else sendError.accept(DMTranslationKeys.TARDIS_NOT_ENOUGH_FUEL);
							} else sendError.accept(new TranslationTextComponent("commands.locatebiome.notFound", biome.get().getRegistryName()));
						} else sendError.accept(new TranslationTextComponent("commands.locatebiome.notFound", biome.get().getRegistryName()));
					} else sendError.accept(new TranslationTextComponent("commands.locatebiome.invalid", tag.getString("Biome")));
				// Waypoint Data Modules
				} else if (tag.contains("location")) {
					CompoundNBT location = tag.getList("location", 10).getCompound(0);
					if (location.contains("pos_x") && location.contains("pos_z")) {
						toLocation.accept(new Vector3d(location.getInt("pos_x"), player.position().y, location.getInt("pos_z")));
					} else sendError.accept(Register.TEXT_INVALID_WAYPOINT);
				// Unknown Data Modules
				} else sendError.accept(Register.TEXT_INVALID_MODULE);
			}
		});
		
		ctx.get().setPacketHandled(true);
	}

}
