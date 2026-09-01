package com.github.argon4w.acceleratedrendering.features.entities;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedEntityShadowRenderer implements IAcceleratedRenderer<AcceleratedEntityShadowRenderer.Context> {

	public static final AcceleratedEntityShadowRenderer INSTANCE = new AcceleratedEntityShadowRenderer();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Context			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		renderFast(
				vertexConsumer,
				vertexConsumer.getAccelerated(),
				context.levelReader(),
				context.chunkAccess(),
				context.blockPos(),
				context.centerX(),
				context.centerY(),
				context.centerZ(),
				context.size(),
				context.weight(),
				transform,
				normal,
				light,
				overlay,
				color
		);
	}

	public void renderFast(
			VertexConsumer				vertexConsumer,
			IAcceleratedVertexConsumer	extension,
			LevelReader					levelReader,
			ChunkAccess				chunkAccess,
			BlockPos					blockPos,
			float						centerX,
			float						centerY,
			float						centerZ,
			float						size,
			float						weight,
			Matrix4f					transform,
			Matrix3f					normal,
			int						light,
			int						overlay,
			int						color
	) {

		var belowPos	= blockPos.below();
		var blockState	= chunkAccess		.getBlockState	(belowPos);

		if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
			return;
		}

		var levelBrightness = levelReader.getMaxLocalRawBrightness(blockPos);

		if (levelBrightness <= 3) {
			return;
		}

		if (!blockState.isCollisionShapeFullBlock(chunkAccess, belowPos)) {
			return;
		}

		var voxelShape = blockState.getShape(chunkAccess, belowPos);

		if (voxelShape.isEmpty()) {
			return;
		}

		var dimensionBrightness	= LightTexture.getBrightness(levelReader.dimensionType(), levelBrightness);
		var shadowTransparency	= weight * 0.5f * dimensionBrightness * 255.0f;

		if (shadowTransparency < 0.0f) {
			return;
		}

		if (shadowTransparency > 255.0f) {
			shadowTransparency = 255.0f;
		}

		var bounds	= voxelShape.bounds	();
		var minX	= blockPos	.getX	() + (float) bounds.minX;
		var maxX	= blockPos	.getX	() + (float) bounds.maxX;
		var minY	= blockPos	.getY	() + (float) bounds.minY;
		var minZ	= blockPos	.getZ	() + (float) bounds.minZ;
		var maxZ	= blockPos	.getZ	() + (float) bounds.maxZ;

		var minPosX = minX - centerX;
		var maxPosX = maxX - centerX;
		var minPosY = minY - centerY;
		var minPosZ = minZ - centerZ;
		var maxPosZ = maxZ - centerZ;

		var u0 = -minPosX / 2.0f / size + 0.5f;
		var u1 = -maxPosX / 2.0f / size + 0.5f;
		var v0 = -minPosZ / 2.0f / size + 0.5f;
		var v1 = -maxPosZ / 2.0f / size + 0.5f;

		extension.beginTransform(transform, normal);

		var red		= FastColor.ARGB32.red	(color) / 255.0f;
		var green	= FastColor.ARGB32.green(color) / 255.0f;
		var blue	= FastColor.ARGB32.blue	(color) / 255.0f;
		var alpha	= shadowTransparency / 255.0f;

		writeVertex(vertexConsumer, minPosX, minPosY, minPosZ, red, green, blue, alpha, u0, v0, overlay, light);
		writeVertex(vertexConsumer, minPosX, minPosY, maxPosZ, red, green, blue, alpha, u0, v1, overlay, light);
		writeVertex(vertexConsumer, maxPosX, minPosY, maxPosZ, red, green, blue, alpha, u1, v1, overlay, light);
		writeVertex(vertexConsumer, maxPosX, minPosY, minPosZ, red, green, blue, alpha, u1, v0, overlay, light);

		extension.endTransform();
	}

	private static void writeVertex(
			VertexConsumer vertexConsumer,
			float x,
			float y,
			float z,
			float red,
			float green,
			float blue,
			float alpha,
			float u,
			float v,
			int overlay,
			int light
	) {
		vertexConsumer.vertex(
				x,
				y,
				z,
				red,
				green,
				blue,
				alpha,
				u,
				v,
				overlay,
				light,
				0.0f,
				1.0f,
				0.0f
		);
	}

	public static Context context(
			LevelReader	levelReader,
			ChunkAccess	chunkAccess,
			BlockPos	blockPos,
			float		centerX,
			float		centerY,
			float		centerZ,
			float		size,
			float		weight
	) {
		return new Context(
				levelReader,
				chunkAccess,
				blockPos,
				centerX,
				centerY,
				centerZ,
				size,
				weight
		);
	}

	public record Context(
			LevelReader	levelReader,
			ChunkAccess	chunkAccess,
			BlockPos	blockPos,
			float		centerX,
			float		centerY,
			float		centerZ,
			float		size,
			float		weight
	) {

	}
}
