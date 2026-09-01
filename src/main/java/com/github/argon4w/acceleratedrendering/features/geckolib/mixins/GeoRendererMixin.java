package com.github.argon4w.acceleratedrendering.features.geckolib.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.utils.CompatibilityMath;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.util.FastColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(IGeoRenderer.class)
public abstract class GeoRendererMixin {

	@SuppressWarnings	("unchecked")
	@Inject				(
			method		= "renderCubesOfBone",
			cancellable	= true,
			at			= @At("HEAD"),
			remap		= false
	)
	void renderCubesOfBoneFast(
			GeoBone			bone,
			PoseStack		poseStack,
			VertexConsumer	buffer,
			int				packedLight,
			int				packedOverlay,
			float			red,
			float			green,
			float			blue,
			float			alpha,
			CallbackInfo	ci
	) {
		var extension = buffer.getAccelerated();

		if (			CoreFeature							.isLoaded						()
				&&		AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&		ModsFeature							.isEnabled						()
				&&		ModsFeature							.shouldAccelerateGecko			()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			var pose = poseStack.last();

			ci			.cancel		();
			extension.doRender	(
					(IAcceleratedRenderer<Void>) bone,
					null,
					CompatibilityMath.toJoml(pose.pose()),
					CompatibilityMath.toJoml(pose.normal()),
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
}
