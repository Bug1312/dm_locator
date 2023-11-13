// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.tiles;

import javax.annotation.Nullable;

import com.bug1312.dm_locator.Register;
import com.swdteam.common.init.DMNBTKeys;
import com.swdteam.common.tileentity.DMTileEntityBase;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class FlightPanelTileEntity extends DMTileEntityBase {
	@Nullable
	public ItemStack cartridge;
	
	public FlightPanelTileEntity() { super(Register.FLIGHT_PANEL_TILE.get()); }

	@Override
	public void load(BlockState state, CompoundNBT compound) {
		if (compound.contains("Item")) this.cartridge = ItemStack.of(compound.getCompound(DMNBTKeys.ITEM));
		
		super.load(state, compound);
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		if (this.cartridge != null && !this.cartridge.isEmpty()) {
			CompoundNBT tag = new CompoundNBT();
			this.cartridge.save(tag);
			compound.put("Item", tag);
		}
		
		return super.save(compound);
	}

}
