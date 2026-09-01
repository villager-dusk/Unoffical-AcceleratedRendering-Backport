package com.github.argon4w.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value= AcceleratedRenderingModEntry.MOD_ID)
public class AcceleratedRenderingModEntry {

	public static final String MOD_ID = "acceleratedrendering";
	public static final Logger LOGGER = LogUtils.getLogger();

	public AcceleratedRenderingModEntry(FMLJavaModLoadingContext context) {
		// Forge 43 (1.19.2) doesn't have FMLJavaModLoadingContext.registerConfig; use ModLoadingContext.
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FeatureConfig.SPEC);
	}
}
