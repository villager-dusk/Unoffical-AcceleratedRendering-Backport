package com.github.argon4w.acceleratedrendering.features.touhoulittlemaid.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoCube;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoQuad;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoVertex;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(GeoBone				.class)
public class GeoBoneMixin implements IAcceleratedRenderer<Void> {

	@Unique private final	Map<IBufferGraph,	IMesh>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final	Map<MeshData,		IMesh>	merges = new Object2ObjectOpenHashMap<>();

	@Unique
	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Void			context,
			Matrix4f		transformMatrix,
			Matrix3f		normalMatrix,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
		var mesh		= meshes		.get			(extension);

		extension.beginTransform(transformMatrix, normalMatrix);

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

		for (GeoCube cube : ((GeoBone) (Object) this).cubes()) {
			for (GeoQuad quad : cube.quads) {
				if (quad == null) {
					continue;
				}

				var normal = quad.normal;

				for (GeoVertex vertex : quad.vertices) {
					meshBuilder.vertex(
							vertex.position.x(),
							vertex.position.y(),
							vertex.position.z(),
							1.0f,
							1.0f,
							1.0f,
							1.0f,
							vertex.textureU,
							vertex.textureV,
							overlay,
							0,
							normal.x(),
							normal.y(),
							normal.z()
					);
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
