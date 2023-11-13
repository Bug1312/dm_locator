// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator;

import com.bug1312.dm_locator.network.PacketRequestLocate;
import com.bug1312.dm_locator.particle.LocateParticle;
import com.swdteam.common.init.DMFlightMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class Events {
	
	@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class CommonModEvents {
		@SubscribeEvent
		public static void setupCommon(FMLCommonSetupEvent event) {
			ModMain.NETWORK.registerMessage(0, PacketRequestLocate.class, PacketRequestLocate::encode, PacketRequestLocate::decode, PacketRequestLocate::handle);
		}
	}
	
	@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void setupClient(FMLClientSetupEvent event) {
			ItemModelsProperties.register(Register.WRITER_ITEM.get(), new ResourceLocation(ModMain.MOD_ID, "writer_mode"),  (stack, world, entity) -> stack.getOrCreateTag().getInt("Mode"));
			
			ClientRegistry.registerKeyBinding(Register.KEYBIND_LOCATE);
		}

		@SubscribeEvent
		public static void registerParticles(ParticleFactoryRegisterEvent event) {
			final Minecraft mc = Minecraft.getInstance();
			final ParticleManager particle = mc.particleEngine;
			
			particle.register(Register.LOCATE_PARTICLE.get(), LocateParticle.Factory::new);
		}
	}
	
	@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ClientForgeEvents {
		private static boolean KEYBIND_LOCATE_DOWN = false;
		
		@SubscribeEvent
		public static void attemptLocate(ClientTickEvent event) {
	        if (event.phase != Phase.END) return;
	        
			if (Register.KEYBIND_LOCATE.isDown() && !KEYBIND_LOCATE_DOWN && DMFlightMode.isInFlight(Minecraft.getInstance().player.getUUID()))
				ModMain.NETWORK.sendToServer(new PacketRequestLocate());
			
			if (Register.KEYBIND_LOCATE.isDown() != KEYBIND_LOCATE_DOWN) 
				KEYBIND_LOCATE_DOWN = Register.KEYBIND_LOCATE.isDown();
		}
	}
}
