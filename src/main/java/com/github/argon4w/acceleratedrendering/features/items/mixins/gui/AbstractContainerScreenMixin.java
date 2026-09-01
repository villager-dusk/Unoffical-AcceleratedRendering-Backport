package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.utils.CompatibilityMath;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

	@Inject(
			method	= "render",
			at		= @At("HEAD")
	)
	public void startBackgroundBatching(
			PoseStack						poseStack,
			int								mouseX,
			int								mouseY,
			float							partialTick,
			CallbackInfo					ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		depth	.set(0.0f);
		enabled	.set(GuiBatchingController.INSTANCE.startBatching(Minecraft.getInstance().renderBuffers().bufferSource()));
	}

	@Inject(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					shift	= At.Shift.BEFORE
			)
	)
	public void flushBackgroundBatching(
			PoseStack						poseStack,
			int								mouseX,
			int								mouseY,
			float							partialTick,
			CallbackInfo					ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (!AcceleratedItemRenderingFeature.shouldMergeGuiItemBatches() && enabled.get()) {
			depth.set(depth.get() + GuiBatchingController.INSTANCE.flushBatching(
					poseStack,
					Minecraft.getInstance().renderBuffers().bufferSource(),
					partialTick
			));

			var pose			= CompatibilityMath.toJoml(poseStack.last().pose());
			var previousDepth	= GuiBatchingController.getGlobalDepth(
					pose.m22(),
					pose.m32(),
					0.0F
			);

			poseStack
					.last		()
					.pose		()
					.multiplyWithTranslation(
							0.0f,
							0.0f,
							depth.get() - previousDepth
					);

			depth.set(0.0f);
		}
	}

	@Inject(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					shift	= At.Shift.AFTER
			)
	)
	public void startItemBatching(
			PoseStack						poseStack,
			int								mouseX,
			int								mouseY,
			float							partialTick,
			CallbackInfo					ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (!AcceleratedItemRenderingFeature.shouldMergeGuiItemBatches() && enabled.get()) {
			GuiBatchingController.INSTANCE.startBatching(Minecraft.getInstance().renderBuffers().bufferSource());
		}
	}

	@Inject(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
					shift	= At.Shift.AFTER
			)
	)
	public void flushItemBatching(
			PoseStack						poseStack,
			int								mouseX,
			int								mouseY,
			float							partialTick,
			CallbackInfo					ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (enabled.get()) {
			depth.set(depth.get() + GuiBatchingController.INSTANCE.flushBatching(
					poseStack,
					Minecraft.getInstance().renderBuffers().bufferSource(),
					partialTick
			));
		}
	}

	@Inject(
			method	= "render",
			at		= @At("TAIL")
	)
	public void liftGlobalLayer(
			PoseStack						poseStack,
			int								mouseX,
			int								mouseY,
			float							partialTick,
			CallbackInfo					ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (enabled.get()) {
			var pose			= CompatibilityMath.toJoml(poseStack.last().pose());
			var previousDepth	= GuiBatchingController.getGlobalDepth(
					pose.m22(),
					pose.m32(),
					0.0F
			);

			poseStack
					.last		()
					.pose		()
					.multiplyWithTranslation(
							0.0f,
							0.0f,
							depth.get() - previousDepth
					);
		}
	}

	@WrapMethod(
			method	= "renderSlotHighlight(Lcom/mojang/blaze3d/vertex/PoseStack;IIII)V",
			remap	= false
	)
	private static void startRenderHighlight(
			PoseStack		poseStack,
			int				highlightX,
			int				highLightY,
			int				blitOffset,
			int				color,
			Operation<Void>	original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			original.call(
					poseStack,
					highlightX,
					highLightY,
					blitOffset,
					color
			);
			return;
		}

		var last = poseStack.last();

		GuiBatchingController.INSTANCE.submitHighlight(
				CompatibilityMath.toJoml(last.pose()),
				CompatibilityMath.toJoml(last.normal()),
				highlightX,
				highLightY,
				blitOffset,
				color
		);
	}
}
