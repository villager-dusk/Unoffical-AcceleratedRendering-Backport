package com.github.argon4w.acceleratedrendering.features.ftb.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiHelper.class)
public class GuiHelperMixin {

	@WrapMethod(
			method	= "drawItem",
			remap	= false
	)
	private static void drawItem(
			PoseStack	poseStack,
			ItemStack	stack,
			int			hash,
			boolean		renderOverlay,
			String		text,
			Operation<Void> original
	) {
		if (		!	stack		.isEmpty			()
				&&		CoreFeature	.isLoaded			()
				&&		ModsFeature	.isEnabled			()
				&&		ModsFeature	.shouldAccelerateFtb()
		) {
			var pose = poseStack;

			pose.pushPose	();
			pose.translate	(
					-8.0D,
					-8.0D,
					-150.0D
			);

			pose
					.last		()
					.normal		()
					.setIdentity	();

			Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 0, 0);

			if (renderOverlay) {
				Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(
						Minecraft.getInstance().font,
						stack,
						0,
						0,
						text
				);
			}

			poseStack.popPose();
		} else {
			original.call(
					poseStack,
					stack,
					hash,
					renderOverlay,
					text
			);
		}
	}
}
