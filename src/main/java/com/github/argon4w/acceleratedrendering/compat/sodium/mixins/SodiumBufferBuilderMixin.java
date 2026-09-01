package com.github.argon4w.acceleratedrendering.compat.sodium.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.EmptyAcceleratedBufferSources;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAccelerationHolder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

// Rubidium 0.6.2c (Sodium 0.4.x for 1.19.2) does not ship a separate entity vertex buffer
// class (it enhances the vanilla BufferBuilder instead), so this mixin is kept as a @Pseudo
// string-targeted hook: it only applies if a SodiumBufferBuilder-like class is present at
// runtime (i.e. a newer Sodium/Embeddium). On Rubidium 0.6.2c it never applies, and entity
// acceleration is already provided by the core BufferBuilderMixin on the vanilla BufferBuilder
// that Rubidium augments. All referenced types are real so the class always compiles/loads.
@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.vertex.buffer.SodiumBufferBuilder")
public class SodiumBufferBuilderMixin implements IAccelerationHolder, IAcceleratedVertexConsumer {

	@Unique private IAcceleratedBufferSource	bufferSources = EmptyAcceleratedBufferSources.INSTANCE;
	@Unique private RenderType					renderType;
	@Unique private AcceleratedBufferBuilder	acceleration;
	@Unique private boolean						init = false;

	@Unique
	@Override
	public VertexConsumer initAcceleration(RenderType renderType, Supplier<IAcceleratedBufferSource> bufferSource) {
		if (CoreFeature.isLoaded() && !init) {
			this.bufferSources	= bufferSource.get();
			this.renderType		= renderType;
			this.acceleration	= null;
			this.init			= true;
		}

		return (VertexConsumer) (Object) this;
	}

	@Unique
	@Override
	public boolean isAccelerated() {
		return bufferSources != EmptyAcceleratedBufferSources.INSTANCE && getAccelerated() != null;
	}

	@Unique
	@Override
	public <T> void doRender(
			IAcceleratedRenderer<T>	renderer,
			T						context,
			Matrix4f				transform,
			Matrix3f				normal,
			int						light,
			int						overlay,
			int						color
	) {
		getAccelerated().doRender(
				renderer,
				context,
				transform,
				normal,
				light,
				overlay,
				color
		);
	}

	@Unique
	@Override
	public AcceleratedBufferBuilder getAccelerated() {
		if (		acceleration == null
				||	acceleration.isOutdated()
		) {
			acceleration = bufferSources.getBuffer(
					renderType,
					CoreFeature.getDefaultLayerBeforeFunction	(),
					CoreFeature.getDefaultLayerAfterFunction	(),
					CoreFeature.getDefaultLayer					()
			);
		}

		return acceleration;
	}
}
