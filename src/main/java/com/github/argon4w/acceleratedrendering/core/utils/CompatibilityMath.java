package com.github.argon4w.acceleratedrendering.core.utils;

import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

// Bridges the math types used by 1.19.2 (com.mojang.math.*) to the ones this mod uses (org.joml.*).
// Minecraft 1.19.2 still uses com.mojang.math matrices/vectors, while this mod was written against the
// 1.20.1 org.joml types. All conversions go through float buffers / accessors.
public final class CompatibilityMath {

	private CompatibilityMath() {}

	public static org.joml.Matrix4f toJoml(com.mojang.math.Matrix4f source) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer buffer = stack.mallocFloat(16);

			source.store(buffer);
			buffer.rewind();

			return new org.joml.Matrix4f(
					buffer.get(), buffer.get(), buffer.get(), buffer.get(),
					buffer.get(), buffer.get(), buffer.get(), buffer.get(),
					buffer.get(), buffer.get(), buffer.get(), buffer.get(),
					buffer.get(), buffer.get(), buffer.get(), buffer.get()
			);
		}
	}

	public static org.joml.Matrix4f toJoml(
			com.mojang.math.Matrix4f source,
			org.joml.Matrix4f destination
	) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer buffer = stack.mallocFloat(16);

			source.store(buffer);
			buffer.rewind();
			destination.set(buffer);

			return destination;
		}
	}

	public static org.joml.Matrix3f toJoml(com.mojang.math.Matrix3f source) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer buffer = stack.mallocFloat(9);

			source.store(buffer);
			buffer.rewind();

			return new org.joml.Matrix3f(
					buffer.get(), buffer.get(), buffer.get(),
					buffer.get(), buffer.get(), buffer.get(),
					buffer.get(), buffer.get(), buffer.get()
			);
		}
	}

	public static org.joml.Matrix3f toJoml(
			com.mojang.math.Matrix3f source,
			org.joml.Matrix3f destination
	) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer buffer = stack.mallocFloat(9);

			source.store(buffer);
			buffer.rewind();
			destination.set(buffer);

			return destination;
		}
	}

	public static org.joml.Quaternionf toJoml(com.mojang.math.Quaternion source) {
		return new org.joml.Quaternionf(source.i(), source.j(), source.k(), source.r());
	}

	public static org.joml.Vector3f toJoml(com.mojang.math.Vector3f source) {
		return new org.joml.Vector3f(source.x(), source.y(), source.z());
	}

	public static com.mojang.math.Matrix4f toMojang(org.joml.Matrix4f source) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final FloatBuffer buffer = stack.mallocFloat(16);

			source.get(buffer);
			buffer.rewind();

			final com.mojang.math.Matrix4f result = new com.mojang.math.Matrix4f();

			result.load(buffer);

			return result;
		}
	}
}
