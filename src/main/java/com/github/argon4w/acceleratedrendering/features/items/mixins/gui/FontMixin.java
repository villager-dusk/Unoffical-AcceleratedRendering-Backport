package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.utils.CompatibilityMath;
import com.github.argon4w.acceleratedrendering.features.items.gui.FontAdvanceEstimator;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Font.class)
public abstract class FontMixin {

	@Shadow public abstract boolean isBidirectional();

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I")
	public int renderGuiStringFast1(
			String					textString,
			float					textX,
			float					textY,
			int						textColor,
			boolean					textShadow,
			com.mojang.math.Matrix4f	transform,
			MultiBufferSource		bufferSource,
			boolean					seeThrough,
			int						backgroundColor,
			int						packedLight,
			Operation<Integer>		original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textString,
					textX,
					textY,
					textColor,
					textShadow,
					transform,
					bufferSource,
					seeThrough,
					backgroundColor,
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new RawStringDrawContext(
				CompatibilityMath.toJoml(transform),
				(Font) (Object) this,
				textString,
				textX,
				textY,
				textColor,
				textShadow,
				seeThrough,
				backgroundColor,
				packedLight,
				isBidirectional()
		));

		return (int) FontAdvanceEstimator.INSTANCE.getAdvance(
				Style.EMPTY,
				textString,
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZIIZ)I")
	public int renderGuiStringFast2(
			String					textString,
			float					textX,
			float					textY,
			int						textColor,
			boolean					textShadow,
			com.mojang.math.Matrix4f	transform,
			MultiBufferSource		bufferSource,
			boolean					seeThrough,
			int						backgroundColor,
			int						packedLight,
			boolean					bidirectional,
			Operation<Integer>		original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textString,
					textX,
					textY,
					textColor,
					textShadow,
					transform,
					bufferSource,
					seeThrough,
					backgroundColor,
					packedLight,
					bidirectional
			);
		}

		GuiBatchingController.INSTANCE.submitString(new RawStringDrawContext(
				CompatibilityMath.toJoml(transform),
				(Font) (Object) this,
				textString,
				textX,
				textY,
				textColor,
				textShadow,
				seeThrough,
				backgroundColor,
				packedLight,
				bidirectional
		));

		return (int) FontAdvanceEstimator.INSTANCE.getAdvance(
				Style.EMPTY,
				textString,
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I")
	public int renderGuiStringFast3(
			Component					textComponent,
			float						textX,
			float						textY,
			int							textColor,
			boolean						textShadow,
			com.mojang.math.Matrix4f	transform,
			MultiBufferSource			bufferSource,
			boolean						seeThrough,
			int							backgroundColor,
			int							packedLight,
			Operation<Integer>			original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textComponent,
					textX,
					textY,
					textColor,
					textShadow,
					transform,
					bufferSource,
					seeThrough,
					backgroundColor,
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new ComponentStringDrawContext(
				CompatibilityMath.toJoml(transform),
				(Font) (Object) this,
				textComponent,
				textX,
				textY,
				textColor,
				textShadow,
				seeThrough,
				backgroundColor,
				packedLight
		));

		return (int) FontAdvanceEstimator.INSTANCE.getAdvance(
				textComponent.getVisualOrderText(),
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I")
	public int renderGuiStringFast4(
			FormattedCharSequence			textSequence,
			float							textX,
			float							textY,
			int								textColor,
			boolean							textShadow,
			com.mojang.math.Matrix4f		transform,
			MultiBufferSource				bufferSource,
			boolean							seeThrough,
			int								backgroundColor,
			int								packedLight,
			Operation<Integer>				original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textSequence,
					textX,
					textY,
					textColor,
					textShadow,
					transform,
					bufferSource,
					seeThrough,
					backgroundColor,
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new FormattedStringDrawContext(
				CompatibilityMath.toJoml(transform),
				(Font) (Object) this,
				textSequence,
				textX,
				textY,
				textColor,
				textShadow,
				seeThrough,
				backgroundColor,
				packedLight
		));

		return (int) FontAdvanceEstimator.INSTANCE.getAdvance(
				textSequence,
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch8xOutline(Lnet/minecraft/util/FormattedCharSequence;FFIILcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
	public void renderGuiStringFast5(
			FormattedCharSequence	text,
			float					textX,
			float					textY,
			int						textColor,
			int						backgroundColor,
			com.mojang.math.Matrix4f	transform,
			MultiBufferSource		bufferSource,
			int						packedLight,
			Operation<Void>			original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			original.call(
					text,
					textX,
					textY,
					textColor,
					backgroundColor,
					transform,
					bufferSource,
					packedLight
			);

			return;
		}

		GuiBatchingController.INSTANCE.submitString(new Outline8StringDrawContext(
				CompatibilityMath.toJoml(transform),
				(Font) (Object) this,
				text,
				textX,
				textY,
				textColor,
				backgroundColor,
				packedLight
		));
	}
}
