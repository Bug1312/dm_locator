// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.mixins;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bug1312.dm_locator.Register;
import com.bug1312.dm_locator.triggers.WriteModuleTrigger.WriteModuleType;
import com.swdteam.common.init.DMDimensions;
import com.swdteam.common.init.DMItems;
import com.swdteam.common.init.DMTardis;
import com.swdteam.common.tardis.TardisData;
import com.swdteam.common.tileentity.tardis.DataWriterTileEntity;
import com.swdteam.network.packets.PacketEjectWaypointCartridge;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketDirection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;

@Mixin(PacketEjectWaypointCartridge.class)
public abstract class TriggerWaypointWrite implements IPacketEjectWaypointCartridgeAccessor {
	
	// An alternative to a mixin is Forge's event for picking up an item.
	// Or maybe there is a smarter way to mixin where you check Context#enqueueWork only in 
	// PacketEjectWaypointCartridge that checks PacketEjectWaypointCartridge::addLocation
	@Inject(at = @At("HEAD"), method = "handle", cancellable = false, remap = false)
	private static void handle(final PacketEjectWaypointCartridge msg, final Supplier<Context> ctx, final CallbackInfo ci) {
		IPacketEjectWaypointCartridgeAccessor mixinMsg = (IPacketEjectWaypointCartridgeAccessor) ((Object) msg);
		ctx.get().enqueueWork(() -> {
			if (ctx.get().getNetworkManager().getDirection() != PacketDirection.SERVERBOUND) return;
			ServerPlayerEntity player = ctx.get().getSender();
			ServerWorld world = player.getServer().getLevel(DMDimensions.TARDIS);
			
			if (player == null || world == null) return;
			TardisData data = DMTardis.getTardisFromInteriorPos(mixinMsg.getBlockPos());
			
			if (data == null || data.getCurrentLocation() == null) return;
			TileEntity te = world.getBlockEntity(mixinMsg.getBlockPos());
			
			if (te == null || !(te instanceof DataWriterTileEntity)) return;
			DataWriterTileEntity writer = (DataWriterTileEntity) te;
			ItemStack stack = writer.cartridge;
			
			if (stack != null && stack.getItem() == DMItems.DATA_MODULE.get() && stack.getOrCreateTag().getBoolean("written")) return;
			if (mixinMsg.getName().startsWith("/")) {
				String[] args = mixinMsg.getName().split(" ");
				if (args.length > 1 && args[0].equalsIgnoreCase("/add")) Register.TRIGGER_WRITE.trigger(player, WriteModuleType.WAYPOINT);
			} else Register.TRIGGER_WRITE.trigger(player, WriteModuleType.WAYPOINT);
		});
	}

}
