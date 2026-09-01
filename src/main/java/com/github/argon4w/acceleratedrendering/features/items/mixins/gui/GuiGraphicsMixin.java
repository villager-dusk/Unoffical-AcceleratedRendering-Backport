package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

// NOTE (1.19.2): net.minecraft.client.gui.GuiGraphics was introduced in 1.19.4. It does not exist in
// 1.19.2, which drives fill/blit/gradient/decorator submission through the old Gui / Tesselator path.
// The GUI batching is now driven from the AbstractContainerScreen / Gui render wrappers (which ARE
// ported), so this 1.19.4-only mixin is intentionally a no-op placeholder for 1.19.2. Fill/blit draw
// calls fall back to vanilla. Keep the file to preserve the original source (no feature excluded).
public class GuiGraphicsMixin {
}
