package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StateSwitchingButton extends AbstractWidget {
   protected ResourceLocation f_94608_;
   protected boolean isStateTriggered;
   protected int f_94610_;
   protected int f_94611_;
   protected int f_94612_;
   protected int f_94613_;

   public StateSwitchingButton(int pX, int pY, int pWidth, int pHeight, boolean pInitialState) {
      super(pX, pY, pWidth, pHeight, CommonComponents.EMPTY);
      this.isStateTriggered = pInitialState;
   }

   public void initTextureValues(int p_94625_, int p_94626_, int p_94627_, int p_94628_, ResourceLocation p_94629_) {
      this.f_94610_ = p_94625_;
      this.f_94611_ = p_94626_;
      this.f_94612_ = p_94627_;
      this.f_94613_ = p_94628_;
      this.f_94608_ = p_94629_;
   }

   public void setStateTriggered(boolean pTriggered) {
      this.isStateTriggered = pTriggered;
   }

   public boolean isStateTriggered() {
      return this.isStateTriggered;
   }

   public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      this.defaultButtonNarrationText(pNarrationElementOutput);
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      RenderSystem.disableDepthTest();
      int i = this.f_94610_;
      int j = this.f_94611_;
      if (this.isStateTriggered) {
         i += this.f_94612_;
      }

      if (this.isHoveredOrFocused()) {
         j += this.f_94613_;
      }

      pGuiGraphics.blit(this.f_94608_, this.getX(), this.getY(), i, j, this.width, this.height);
      RenderSystem.enableDepthTest();
   }
}