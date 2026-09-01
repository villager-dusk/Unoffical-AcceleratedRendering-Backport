package com.github.argon4w.acceleratedrendering.core.mixins.buffers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.SheetedDecalTextureRenderer;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import lombok.experimental.ExtensionMethod;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.FloatBuffer;

@ExtensionMethod(VertexConsumerExtension		.class)
@Mixin			(SheetedDecalTextureGenerator	.class)
public class SheetedDecalTextureGeneratorMixin implements IAcceleratedVertexConsumer {

	@Shadow @Final private VertexConsumer	delegate;
	@Shadow @Final private com.mojang.math.Matrix4f	cameraInversePose;
	@Shadow @Final private com.mojang.math.Matrix3f	normalInversePose;

	@Unique
	@Override
	public boolean isAccelerated() {
		return delegate
				.getAccelerated()
				.isAccelerated();
	}

	@Unique
	@Override
	public <T>  void doRender(
			IAcceleratedRenderer<T>	renderer,
			T						context,
			org.joml.Matrix4f		transform,
			org.joml.Matrix3f		normal,
			int						light,
			int						overlay,
			int						color
	) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			var cam = stack.mallocFloat(16);
			var nrm = stack.mallocFloat(9);

			cameraInversePose	.store	(cam);
			normalInversePose	.store	(nrm);
			cam.rewind	();
			nrm.rewind	();

			delegate
					.getAccelerated	()
					.doRender		(
							new SheetedDecalTextureRenderer<>(
									renderer,
									new org.joml.Matrix4f(cam),
									new org.joml.Matrix3f(nrm),
									1.0f
							),
							context,
							transform,
							normal,
							light,
							overlay,
							color
					);
		}
	}
}
