package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageWidget extends AbstractWidget {
   private final ResourceLocation f_273859_;

   public ImageWidget(int pX, int pY, ResourceLocation p_275649_) {
      this(0, 0, pX, pY, p_275649_);
   }

   public ImageWidget(int p_275421_, int p_275294_, int p_275403_, int p_275631_, ResourceLocation p_275648_) {
      super(p_275421_, p_275294_, p_275403_, p_275631_, Component.empty());
      this.f_273859_ = p_275648_;
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
   }

   public void renderWidget(GuiGraphics p_283475_, int p_281265_, int p_281555_, float p_282690_) {
      int i = this.getWidth();
      int j = this.getHeight();
      p_283475_.blit(this.f_273859_, this.getX(), this.getY(), 0.0F, 0.0F, i, j, i, j);
   }
}