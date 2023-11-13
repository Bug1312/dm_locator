// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.mixins;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bug1312.dm_locator.StructureHelper;
import com.swdteam.common.init.DMFlightMode;

@Mixin(DMFlightMode.class)
public class DMFlightCancelWatch {
	
	@Inject(at = @At("TAIL"), method = "removeFlight(Ljava/util/UUID;Z)V", remap = false)
	private static void removeFlight(final UUID player, final boolean isClient, final CallbackInfo ci) {
		if (!isClient) StructureHelper.FLYING_PLAYERS_LOCATOR.remove(player);
	}
}
