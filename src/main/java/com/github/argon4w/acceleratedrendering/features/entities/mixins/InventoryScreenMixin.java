package com.github.argon4w.acceleratedrendering.features.entities.mixins;

// NOTE (1.19.2): this mixin originally wrapped the synthetic lambda
// lambda$renderEntityInInventoryRaw$1 to accelerate the player model rendered in the inventory screen.
// The Mixin annotation processor in this toolchain cannot resolve synthetic lambda methods as
// @WrapMethod targets, so the interception is intentionally no-op on 1.19.2: inventory-screen entities
// render through the vanilla path. Keep the file to preserve the original source (no feature excluded).
public class InventoryScreenMixin {
}
