// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.bug1312.dm_locator.Config;
import com.bug1312.dm_locator.Register;
import com.bug1312.dm_locator.StructureHelper;
import com.bug1312.dm_locator.triggers.WriteModuleTrigger.WriteModuleType;
import com.swdteam.common.init.DMItems;
import com.swdteam.common.item.DataModuleItem;
import com.swdteam.util.ItemUtils;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LocatorDataWriterItem extends Item {

	private static final Predicate<ItemStack> WRITABLE = (stack) -> (
		stack.getItem() instanceof DataModuleItem && 
		!(
			stack.getItem() == DMItems.DATA_MODULE.get() &&
			stack.getOrCreateTag().contains("written")
		)
	);
			
	public LocatorDataWriterItem(Properties properties) {
		super(properties);
	}
		
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		LocatorDataWriterMode mode = getMode(player.getItemInHand(hand));
		
		if (player.isShiftKeyDown()) {
			player.getItemInHand(hand).getOrCreateTag().putInt("Mode", (mode.ordinal() + 1) % LocatorDataWriterMode.values().length);
			player.displayClientMessage(Register.TEXT_SWAP_MODE.apply(getMode(player.getItemInHand(hand))).setStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), true);
			
			return ActionResult.consume(player.getItemInHand(hand));
		}
		
		Consumer<TranslationTextComponent> sendError = (text) -> player.displayClientMessage(text.setStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
		ItemStack module = getModule(player);
		
		if (!module.isEmpty()) {
			if (!(module.getItem() == DMItems.DATA_MODULE.get() && module.getOrCreateTag().contains("written"))) {
				if (!world.isClientSide()) {
					switch (mode) {
						case STRUCTURE:
							Optional<ResourceLocation> structure = StructureHelper.getStructure((ServerPlayerEntity) player);
							if (structure.isPresent() && !Config.SERVER_CONFIG.getStructureBlacklist().contains(structure.get())) {
								ItemStack newModule = module.split(1);
								if (player.abilities.instabuild) module.grow(1);

								CompoundNBT tag = setupModuleTag(newModule.getOrCreateTag());

								tag.putString("Structure", structure.get().toString());
								String name = StructureHelper.formatResourceLocationName(structure.get());
								
								newModule.setHoverName(Register.TEXT_MODULE_NAME.apply(mode, name).setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).withItalic(false)));
		                        tag.putString("cart_name", name);

								Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
								if (player.getItemInHand(otherHand) == module && module.isEmpty()) player.setItemInHand(otherHand, newModule);
								else player.addItem(newModule);
								
								Register.TRIGGER_WRITE.trigger((ServerPlayerEntity) player, WriteModuleType.STRUCTURE);
							} else sendError.accept(Register.TEXT_NO_STRUCTURE);
							break;
						case BIOME:
							ResourceLocation biome = world.getBiome(player.blockPosition()).getRegistryName();
							if (!Config.SERVER_CONFIG.getBiomeBlacklist().contains(biome)) {
								ItemStack newModule = module.split(1);
								if (player.abilities.instabuild) module.grow(1);

								CompoundNBT tag = setupModuleTag(newModule.getOrCreateTag());

								tag.putString("Biome", biome.toString());
								String name = StructureHelper.formatResourceLocationName(biome);
								
								newModule.setHoverName(Register.TEXT_MODULE_NAME.apply(mode, name).setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).withItalic(false)));
		                        tag.putString("cart_name", name);

								Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
								if (player.getItemInHand(otherHand) == module && module.isEmpty()) player.setItemInHand(otherHand, newModule);
								else player.addItem(newModule);
								
								Register.TRIGGER_WRITE.trigger((ServerPlayerEntity) player, WriteModuleType.BIOME);
							} else sendError.accept(Register.TEXT_INVALID_BIOME);
							break;
					}
				}
				return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
			} else if (world.isClientSide()) sendError.accept(Register.TEXT_MODULE_WRITTEN);
		} else if (world.isClientSide()) sendError.accept(Register.TEXT_NO_MODULES);
		
		return ActionResult.fail(player.getItemInHand(hand));
	}
	
	@Override @OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		ItemUtils.addText(tooltip, Register.TEXT_TOOLTIP_MODE.apply(getMode(stack)).getString(), TextFormatting.GREEN);
	}
	
	private static LocatorDataWriterMode getMode(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		return LocatorDataWriterMode.values()[tag.getInt("Mode") % LocatorDataWriterMode.values().length];
	}
	
	private static ItemStack getModule(PlayerEntity player) {
		// hands
		if (player.getItemInHand(Hand.OFF_HAND).getItem() instanceof DataModuleItem) return player.getItemInHand(Hand.OFF_HAND);
		if (player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof DataModuleItem) return player.getItemInHand(Hand.MAIN_HAND);
		
		// inventory
		List<ItemStack> modules = new ArrayList<>();
		for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
			ItemStack invStack = player.inventory.getItem(i);
			if (WRITABLE.test(invStack)) {
				modules.add(invStack);
				if (!invStack.getOrCreateTag().contains("written")) return invStack;
			}
		}
		if (modules.size() > 0) return modules.get(0);
		
		// creative mode
		if (player.abilities.instabuild) return new ItemStack(DMItems.DATA_MODULE.get());
		
		return ItemStack.EMPTY;
	}
	
	private static CompoundNBT setupModuleTag(CompoundNBT tag) {
		tag.putBoolean("written", true);
		tag.remove("location");
		tag.remove("Structure");
		tag.remove("Biome");
		return tag;
	}
	
	public static enum LocatorDataWriterMode { STRUCTURE, BIOME; };
	
}
