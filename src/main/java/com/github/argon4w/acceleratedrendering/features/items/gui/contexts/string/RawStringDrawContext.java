package com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string;

import com.github.argon4w.acceleratedrendering.core.utils.CompatibilityMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record RawStringDrawContext(
		Matrix4f			transform,
		Font				font,
		String				text,
		float				textX,
		float				textY,
		int					textColor,
		boolean				dropShadow,
		boolean				seeThrough,
		int					backgroundColor,
		int					packedLight,
		boolean				bidirectional
) implements IStringDrawContext {

	@Override
	public void drawString(MultiBufferSource bufferSource) {
		font.drawInBatch(
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				CompatibilityMath.toMojang(transform),
				bufferSource,
				seeThrough,
				backgroundColor,
				packedLight,
				bidirectional
		);
	}
}
