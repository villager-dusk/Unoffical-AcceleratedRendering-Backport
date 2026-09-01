package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

	@Inject(
			method	= "renderHotbar",
			at		= @At("HEAD")
	)
	public void startBatching(
			float			partialTicks,
			PoseStack		poseStack,
			CallbackInfo	ci
	) {
		GuiBatchingController.INSTANCE.startBatching(Minecraft.getInstance().renderBuffers().bufferSource());
	}

	@Inject(
			method	= "renderHotbar",
			at		= @At("TAIL")
	)
	public void flushBatching(
			float			partialTicks,
			PoseStack		poseStack,
			CallbackInfo	ci
	) {
		GuiBatchingController.INSTANCE.flushBatching(
				poseStack,
				Minecraft.getInstance().renderBuffers().bufferSource(),
				partialTicks
		);
	}
}
