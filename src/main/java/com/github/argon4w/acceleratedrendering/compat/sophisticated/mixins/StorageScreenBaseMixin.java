package com.github.argon4w.acceleratedrendering.compat.sophisticated.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.utils.CompatibilityMath;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(StorageScreenBase.class)
public class StorageScreenBaseMixin {

	@Inject(
			method	= "renderSuper",
			at		= @At("HEAD"),
			remap	= false
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
		if (		CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	()
		) {
			depth	.set(0.0f);
			enabled	.set(GuiBatchingController.INSTANCE.startBatching(Minecraft.getInstance().renderBuffers().bufferSource()));
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					shift	= At.Shift.BEFORE,
					ordinal	= 0
			),
			remap	= false
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
		if (		CoreFeature						.isLoaded						()
				&&	ModsFeature						.isEnabled						()
				&&	ModsFeature						.shouldAccelerateSophisticated	()
				&& !AcceleratedItemRenderingFeature	.shouldMergeGuiItemBatches		()
				&&	enabled							.get							()
		) {
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
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					shift	= At.Shift.AFTER,
					ordinal	= 0
			),
			remap	= false
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
		if (		CoreFeature						.isLoaded						()
				&&	ModsFeature						.isEnabled						()
				&&	ModsFeature						.shouldAccelerateSophisticated	()
				&& !AcceleratedItemRenderingFeature	.shouldMergeGuiItemBatches		()
				&&	enabled							.get							()
		) {
			GuiBatchingController.INSTANCE.startBatching(Minecraft.getInstance().renderBuffers().bufferSource());
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V",
					shift	= At.Shift.AFTER,
					ordinal	= 1
			),
			remap	= false
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
		if (		CoreFeature	.isLoaded						()
				&&	ModsFeature	.isEnabled						()
				&&	ModsFeature	.shouldAccelerateSophisticated	()
				&&	enabled		.get							()
		) {
			depth.set(depth.get() + GuiBatchingController.INSTANCE.flushBatching(
					poseStack,
					Minecraft.getInstance().renderBuffers().bufferSource(),
					partialTick
			));
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At("TAIL"),
			remap	= false
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
}
