package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class PoseStackExtension {

	public static void setPose(
			PoseStack	in,
			Matrix4f	transform,
			Matrix3f	normal
	) {
		var last = in.last();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer pose	= stack.mallocFloat(16);
			final FloatBuffer norm	= stack.mallocFloat(9);

			transform	.get(pose);	pose.rewind();
			normal		.get(norm);	norm.rewind();

			last.pose	().load(pose);
			last.normal	().load(norm);
		}
	}
}
