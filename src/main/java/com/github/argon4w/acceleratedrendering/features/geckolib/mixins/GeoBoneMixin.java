package com.github.argon4w.acceleratedrendering.features.geckolib.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.util.RenderUtils;

import java.util.List;
import java.util.Map;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(GeoBone				.class)
public class GeoBoneMixin implements IAcceleratedRenderer<Void> {

	@Shadow(remap = false) public			List<GeoCube>	childCubes;

	@Unique private	final	Map<IBufferGraph,	IMesh>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final	Map<MeshData,		IMesh>	merges = new Object2ObjectOpenHashMap<>();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Void			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
		var mesh		= meshes		.get			(extension);

		extension.beginTransform(transform, normal);

		if (mesh != null) {
			mesh.write(
					extension,
					color,
					light,
					overlay
			);

			extension.endTransform();
			return;
		}

		var meshCollector	= CoreFeature	.createMeshCollector(extension);
		var meshBuilder		= extension		.decorate			(meshCollector);

		for (GeoCube cube : childCubes) {
			var poseStack = new PoseStack();

			RenderUtils.translateToPivotPoint		(poseStack, cube);
			RenderUtils.rotateMatrixAroundCube		(poseStack, cube);
			RenderUtils.translateAwayFromPivotPoint	(poseStack, cube);

			var pose			= poseStack	.last	();
			com.mojang.math.Matrix4f cubeTransform	= pose		.pose	();
			com.mojang.math.Matrix3f cubeNormal		= pose		.normal	();

			for (GeoQuad quad : cube.quads) {
				if (quad != null) {
					com.mojang.math.Vector3f polygonNormal = quad.normal.copy();
					polygonNormal.transform(cubeNormal);

					for (GeoVertex vertex : quad.vertices) {
						com.mojang.math.Vector4f vertexPosition = new com.mojang.math.Vector4f(
								vertex.position.x(),
								vertex.position.y(),
								vertex.position.z(),
								1.0f
						);
						vertexPosition.transform(cubeTransform);

						meshBuilder.vertex(
								vertexPosition.x(),
								vertexPosition.y(),
								vertexPosition.z(),
								1.0f,
								1.0f,
								1.0f,
								1.0f,
								vertex.textureU,
								vertex.textureV,
								overlay,
								0,
								polygonNormal.x(),
								polygonNormal.y(),
								polygonNormal.z()
						);
					}
				}
			}
		}

		meshCollector.flush();

		var data	= meshCollector	.getData	();
		var buffer	= meshCollector	.getBuffer	();
		mesh		= merges		.get		(data);

		if (mesh != null) {
			buffer.close();
		} else {
			mesh = AcceleratedEntityRenderingFeature
					.getMeshType()
					.getBuilder	()
					.build		(meshCollector);
		}

		meshes	.put	(extension, mesh);
		merges	.put	(data,		mesh);
		mesh	.write	(
				extension,
				color,
				light,
				overlay
		);

		extension.endTransform();
	}
}
