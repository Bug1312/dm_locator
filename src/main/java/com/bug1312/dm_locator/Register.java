// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.bug1312.dm_locator.items.LocatorDataWriterItem;
import com.bug1312.dm_locator.items.LocatorDataWriterItem.LocatorDataWriterMode;
import com.bug1312.dm_locator.loot_modifiers.FlightPanelLootModifier;
import com.bug1312.dm_locator.particle.LocateParticleData;
import com.bug1312.dm_locator.tiles.FlightPanelTileEntity;
import com.bug1312.dm_locator.triggers.UseLocatorTrigger;
import com.bug1312.dm_locator.triggers.WriteModuleTrigger;
import com.mojang.serialization.Codec;
import com.swdteam.common.init.DMBlocks;
import com.swdteam.common.init.DMTabs;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Register {
	// Items
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);
	public static final RegistryObject<Item> WRITER_ITEM = register(ITEMS, "locator_data_writer", () -> new LocatorDataWriterItem(new Properties().tab(DMTabs.DM_TARDIS).stacksTo(1)));
	public static final RegistryObject<Item> LOCATOR_ATTACHMENT_ITEM = register(ITEMS, "locator_attachment", () -> new Item(new Properties().tab(DMTabs.DM_TARDIS)));
	
	// Tiles
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ModMain.MOD_ID);
	public static final RegistryObject<TileEntityType<?>> FLIGHT_PANEL_TILE = register(TILE_ENTITIES, "flight_panel", () -> Builder.of(FlightPanelTileEntity::new, DMBlocks.FLIGHT_PANEL.get()).build(null));

    // Loot Modifiers
	public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, ModMain.MOD_ID);
    public static final RegistryObject<FlightPanelLootModifier.Serializer> FLIGHT_PANEL_LOOT_MODIFIER = register(LOOT_MODIFIERS, "flight_panel", FlightPanelLootModifier.Serializer::new);

	// Particles
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ModMain.MOD_ID);
	public static final RegistryObject<ParticleType<LocateParticleData>> LOCATE_PARTICLE = register(PARTICLE_TYPES, "locate", () -> 
		new ParticleType<LocateParticleData>(false, LocateParticleData.DESERIALIZER) {
			public Codec<LocateParticleData> codec() { return LocateParticleData.CODEC;	}
		}
	);
	
	// Advancement Triggers
	public static final WriteModuleTrigger TRIGGER_WRITE = CriteriaTriggers.register(new WriteModuleTrigger());
	public static final UseLocatorTrigger TRIGGER_USE = CriteriaTriggers.register(new UseLocatorTrigger());

	// Key Binds
    public static final KeyBinding KEYBIND_LOCATE = new KeyBinding(String.format("%s.keybinds.locate_blast", ModMain.MOD_ID), 66, "Dalek Mod"); // B
    
    // Translations
    public static final Function<LocatorDataWriterMode, TranslationTextComponent> TEXT_SWAP_MODE = (m) -> new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.swap_mode.%s", ModMain.MOD_ID, m));
    public static final Function<LocatorDataWriterMode, TranslationTextComponent> TEXT_TOOLTIP_MODE = (m) -> new TranslationTextComponent(String.format("item.%s.locator_data_writer.hover.mode.%s", ModMain.MOD_ID, m));
    public static final TranslationTextComponent TEXT_INSUFFICIENT_FUEL = new TranslationTextComponent(String.format("tooltip.%s.flight_mode.insufficient_fuel", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_INVALID_STRUCTURE = new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.invalid_structure", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_INVALID_BIOME = new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.invalid_biome", ModMain.MOD_ID));
    public static final TranslationTextComponent TEXT_INVALID_WAYPOINT = new TranslationTextComponent(String.format("tooltip.%s.flight_mode.invalid_waypoint", ModMain.MOD_ID));
    public static final TranslationTextComponent TEXT_INVALID_MODULE = new TranslationTextComponent(String.format("tooltip.%s.flight_mode.invalid_module", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_NO_STRUCTURE = new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.no_structures", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_NO_BIOME = new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.no_biome", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_NO_MODULES = new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.no_modules", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_MODULE_WRITTEN = new TranslationTextComponent(String.format("tooltip.%s.locator_data_writer.module_written", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_PANEL_LOAD = new TranslationTextComponent(String.format("tooltip.%s.flight_panel.load", ModMain.MOD_ID));
	public static final TranslationTextComponent TEXT_PANEL_EJECT = new TranslationTextComponent(String.format("tooltip.%s.flight_panel.eject", ModMain.MOD_ID));
    public static final Supplier<TranslationTextComponent> TEXT_USE_BUTTON = () -> new TranslationTextComponent(String.format("overlay.%s.flight_mode.use_button", ModMain.MOD_ID), new KeybindTextComponent(Register.KEYBIND_LOCATE.getName()));
    public static final BiFunction<LocatorDataWriterMode, String, TranslationTextComponent> TEXT_MODULE_NAME = (m, s) -> new TranslationTextComponent(String.format("name.%s.structure_module.%s", ModMain.MOD_ID, m), s);

    // Register Method
	public static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> register(final DeferredRegister<T> register, final String name, final Supplier<U> supplier) {
		return register.register(name, supplier);
	}
}
