// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bug1312.dm_locator.Register;
import com.bug1312.dm_locator.StructureHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.swdteam.client.overlay.OverlayFlightMode;
import com.swdteam.common.init.DMFlightMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(OverlayFlightMode.class)
public class OverlayFlightModeMixin {

	@Inject(at = @At("TAIL"), method = "render", remap = false)
	public void render(final MatrixStack stack, final CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		FontRenderer font = mc.font;
		PlayerEntity player = mc.player;
				
		if (player != null && StructureHelper.isFlyingWithLocator && DMFlightMode.isInFlight(player)) {
			font.drawShadow(stack, Register.TEXT_USE_BUTTON.get(), 8.0F, 80.0F, -659185);
		}
	}

}
