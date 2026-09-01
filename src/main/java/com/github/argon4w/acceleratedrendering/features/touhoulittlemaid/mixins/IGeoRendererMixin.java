package com.github.argon4w.acceleratedrendering.features.touhoulittlemaid.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.utils.CompatibilityMath;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.FastColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(IGeoRenderer			.class)
public abstract class IGeoRendererMixin {

	@SuppressWarnings	("unchecked")
	@Inject				(
			method		= "renderRecursively",
			at			= @At("HEAD"),
			cancellable	= true,
			remap		= false
	)
	private void renderRecursivelyFast(
			AnimatedGeoBone		bone,
			PoseStack			poseStack,
			VertexConsumer		buffer,
			int					packedLight,
			int					packedOverlay,
			float				red,
			float				green,
			float				blue,
			float				alpha,
			CallbackInfo		ci
	) {
		var extension = buffer.getAccelerated();

		if (			CoreFeature							.isLoaded						()
				&&		AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&		ModsFeature							.isEnabled						()
				&&		ModsFeature							.shouldAccelerateTlm			()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			ci.cancel();

			renderRecursivelyFast(
					bone,
					poseStack,
					extension,
					packedLight,
					packedOverlay,
					FastColor.ARGB32.color(
							(int) (alpha	* 255.0f),
							(int) (red		* 255.0f),
							(int) (green	* 255.0f),
							(int) (blue		* 255.0f)
					)
			);
		}
	}

	@Unique
	@SuppressWarnings("unchecked")
	private static void renderRecursivelyFast(
			AnimatedGeoBone					animated,
			PoseStack						poseStack,
			IAcceleratedVertexConsumer		extension,
			int								packedLight,
			int								packedOverlay,
			int								packedColor
	) {
		if (		(animated.getScaleX() == 0.0F ? 0 : 1)
				+	(animated.getScaleY() == 0.0F ? 0 : 1)
				+	(animated.getScaleZ() == 0.0F ? 0 : 1) >= 2
		) {
			poseStack.pushPose();

			RenderUtils.prepMatrixForBone(poseStack, animated);

			if (		!animated.isHidden		()
					&&	!animated.cubesAreHidden()
			) {
				var last = poseStack.last	();
				var bone = animated	.geoBone();

				extension.doRender(
						(IAcceleratedRenderer<Void>) bone,
						null,
						CompatibilityMath.toJoml(last.pose()),
						CompatibilityMath.toJoml(last.normal()),
						bone.glow() ? LightTexture.FULL_BRIGHT : packedLight,
						packedOverlay,
						packedColor
				);
			}

			if (!animated.childBonesAreHiddenToo()) {
				for (var childBone : animated.children()) {
					renderRecursivelyFast(
							childBone,
							poseStack,
							extension,
							packedLight,
							packedOverlay,
							packedColor
					);
				}
			}

			poseStack.popPose();
		}
	}
}
