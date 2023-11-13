// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.swdteam.client.render.RenderEvents;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

@Mixin(RenderEvents.class)
public class FixDMFlight {
	// I keep getting mad at the constant crashing from not exiting MC safely
	// and ruining an entire world because Event#isCancelable wasn't checked.
	@Inject(at = @At("HEAD"), method = "renderGameOverlay", cancellable = true, remap = false)
	private static void cantCancelEvent(final RenderGameOverlayEvent event, final CallbackInfo ci) {
		if (!event.isCancelable()) ci.cancel();
	}
}
