// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(ModMain.MOD_ID)
public class ModMain {
	public static final String MOD_ID = "dm_locator";
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(MOD_ID, "main"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();
	
	public ModMain() {
		ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_SPEC, MOD_ID + "-server.toml");
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		Register.ITEMS.register(modBus);
		Register.PARTICLE_TYPES.register(modBus);
		Register.TILE_ENTITIES.register(modBus);
		Register.LOOT_MODIFIERS.register(modBus);
	}

}